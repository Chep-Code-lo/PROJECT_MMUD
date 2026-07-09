param(
    [string]$BaseUrl = "",
    [string]$EnvFile = ".env"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"
[System.Net.ServicePointManager]::ServerCertificateValidationCallback = { $true }

$script:results = New-Object System.Collections.ArrayList
$repoRoot = Split-Path -Parent $PSScriptRoot
$envPath = Join-Path $repoRoot $EnvFile

function Write-Step {
    param([string]$Message)

    Write-Host ""
    Write-Host "== $Message ==" -ForegroundColor Cyan
}

function Add-Result {
    param(
        [string]$Scenario,
        [object]$Expected,
        [object]$Actual,
        [string]$Note
    )

    $expectedString = [string]$Expected
    $actualString = [string]$Actual
    $outcome = if ($expectedString -eq $actualString) { "PASS" } else { "FAIL" }

    [void]$script:results.Add([pscustomobject]@{
        Scenario = $Scenario
        Expected = $expectedString
        Actual   = $actualString
        Outcome  = $outcome
        Note     = $Note
    })

    if ($outcome -eq "PASS") {
        Write-Host "[PASS] $Scenario -> $actualString" -ForegroundColor Green
        return
    }

    Write-Host "[FAIL] $Scenario -> expected $expectedString but got $actualString" -ForegroundColor Red
    if ($Note) {
        Write-Host "       $Note" -ForegroundColor DarkYellow
    }
    throw "Security demo stopped at scenario: $Scenario"
}

function Read-DotEnv {
    param([string]$Path)

    $values = @{}
    if (-not (Test-Path $Path)) {
        return $values
    }

    foreach ($line in Get-Content $Path) {
        $trimmed = $line.Trim()
        if (-not $trimmed -or $trimmed.StartsWith("#")) {
            continue
        }

        $parts = $line -split "=", 2
        if ($parts.Count -ne 2) {
            continue
        }

        $name = $parts[0].Trim()
        $value = $parts[1].Trim()
        if ($value.Length -ge 2) {
            if (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'"))) {
                $value = $value.Substring(1, $value.Length - 2)
            }
        }

        $values[$name] = $value
    }

    return $values
}

function Get-ConfigValue {
    param(
        [hashtable]$Values,
        [string]$Name,
        [string]$Default = ""
    )

    if ($Values.ContainsKey($Name) -and -not [string]::IsNullOrWhiteSpace($Values[$Name])) {
        return $Values[$Name]
    }

    $environmentValue = [Environment]::GetEnvironmentVariable($Name)
    if (-not [string]::IsNullOrWhiteSpace($environmentValue)) {
        return $environmentValue
    }

    return $Default
}

function Parse-JsonMaybe {
    param([string]$Raw)

    if ([string]::IsNullOrWhiteSpace($Raw)) {
        return $null
    }

    try {
        return $Raw | ConvertFrom-Json
    } catch {
        return $Raw
    }
}

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        [hashtable]$Headers = @{},
        [object]$Body,
        [string]$RawBody
    )

    $uri = if ($Path.StartsWith("http")) { $Path } else { ($BaseUrl.TrimEnd("/") + $Path) }
    $requestHeaders = @{}
    foreach ($key in $Headers.Keys) {
        $requestHeaders[$key] = $Headers[$key]
    }

    $curlArguments = @(
        "-k",
        "-sS",
        "-X",
        $Method,
        $uri,
        "-w",
        "`n__STATUS__:%{http_code}"
    )

    foreach ($key in $requestHeaders.Keys) {
        $curlArguments += @("-H", "${key}: $($requestHeaders[$key])")
    }

    $temporaryBodyPath = $null
    try {
        if ($PSBoundParameters.ContainsKey("RawBody")) {
            $temporaryBodyPath = [System.IO.Path]::GetTempFileName()
            [System.IO.File]::WriteAllText($temporaryBodyPath, $RawBody, (New-Object System.Text.UTF8Encoding $false))
            $curlArguments += @("-H", "Content-Type: application/json", "--data-binary", "@$temporaryBodyPath")
        } elseif ($PSBoundParameters.ContainsKey("Body") -and $null -ne $Body) {
            $jsonBody = $Body | ConvertTo-Json -Depth 10 -Compress
            $temporaryBodyPath = [System.IO.Path]::GetTempFileName()
            [System.IO.File]::WriteAllText($temporaryBodyPath, $jsonBody, (New-Object System.Text.UTF8Encoding $false))
            $curlArguments += @("-H", "Content-Type: application/json", "--data-binary", "@$temporaryBodyPath")
        }

        $rawOutput = & curl.exe @curlArguments
        if ($LASTEXITCODE -ne 0) {
            throw "curl.exe failed for $Method $uri"
        }
    } finally {
        if ($temporaryBodyPath -and (Test-Path $temporaryBodyPath)) {
            Remove-Item $temporaryBodyPath -Force
        }
    }

    $rawText = [string]::Join("`n", $rawOutput)
    $statusMarker = "__STATUS__:"
    $statusIndex = $rawText.LastIndexOf($statusMarker)
    if ($statusIndex -lt 0) {
        throw "Unable to parse HTTP status for $Method $uri"
    }

    $rawBody = $rawText.Substring(0, $statusIndex).Trim()
    $statusCode = $rawText.Substring($statusIndex + $statusMarker.Length).Trim()

    return [pscustomobject]@{
        StatusCode = [int]$statusCode
        RawBody    = $rawBody
        Body       = Parse-JsonMaybe $rawBody
        Headers    = @{}
    }
}

function ConvertTo-Base64Url {
    param([byte[]]$Bytes)

    return [Convert]::ToBase64String($Bytes).TrimEnd("=").Replace("+", "-").Replace("/", "_")
}

function New-SignedJwt {
    param(
        [hashtable]$Payload,
        [string]$Secret
    )

    $header = @{ alg = "HS256"; typ = "JWT" }
    $headerJson = $header | ConvertTo-Json -Compress
    $payloadJson = $Payload | ConvertTo-Json -Compress

    $headerPart = ConvertTo-Base64Url ([System.Text.Encoding]::UTF8.GetBytes($headerJson))
    $payloadPart = ConvertTo-Base64Url ([System.Text.Encoding]::UTF8.GetBytes($payloadJson))
    $unsignedToken = "$headerPart.$payloadPart"

    $sha256 = [System.Security.Cryptography.SHA256]::Create()
    try {
        $keyBytes = $sha256.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($Secret))
    } finally {
        $sha256.Dispose()
    }

    $hmac = New-Object System.Security.Cryptography.HMACSHA256
    try {
        $hmac.Key = $keyBytes
        $signatureBytes = $hmac.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($unsignedToken))
    } finally {
        $hmac.Dispose()
    }

    return "$unsignedToken.$(ConvertTo-Base64Url $signatureBytes)"
}

function Invoke-DbQuery {
    param([string]$Sql)

    $dockerArguments = @(
        "exec",
        "-e",
        "MYSQL_PWD=$script:mysqlAppPassword",
        "securityapp-db",
        "mysql",
        "-B",
        "-N",
        "-u$script:mysqlAppUser",
        "-D",
        $script:mysqlDatabase,
        "-e",
        $Sql
    )

    $output = & docker @dockerArguments
    if ($LASTEXITCODE -ne 0) {
        throw "MySQL query failed: $Sql"
    }

    $lines = @()
    foreach ($line in @($output)) {
        $text = [string]$line
        if ([string]::IsNullOrWhiteSpace($text) -or $text.StartsWith("mysql: [Warning]")) {
            continue
        }
        $lines += $text
    }

    return ,$lines
}

function Convert-PlainObject {
    param([object]$Value)

    if ($null -eq $Value) {
        return $null
    }

    return $Value | ConvertTo-Json -Depth 10 | ConvertFrom-Json
}

$envValues = Read-DotEnv $envPath
$BaseUrl = if ([string]::IsNullOrWhiteSpace($BaseUrl)) {
    Get-ConfigValue -Values $envValues -Name "APP_API_BASE_URL" -Default "https://localhost"
} else {
    $BaseUrl
}
$jwtSecret = Get-ConfigValue -Values $envValues -Name "JWT_SECRET"
$script:mysqlDatabase = Get-ConfigValue -Values $envValues -Name "MYSQL_DATABASE" -Default "securityapp"
$script:mysqlAppUser = Get-ConfigValue -Values $envValues -Name "MYSQL_APP_USERNAME" -Default "securityapp"
$script:mysqlAppPassword = Get-ConfigValue -Values $envValues -Name "MYSQL_APP_PASSWORD"

if ([string]::IsNullOrWhiteSpace($jwtSecret) -or [string]::IsNullOrWhiteSpace($script:mysqlAppPassword)) {
    throw "Missing JWT_SECRET or MYSQL_APP_PASSWORD in .env."
}

Write-Step "Health check"
$healthResponse = Invoke-Api -Method "GET" -Path "/api/health"
Add-Result -Scenario "API health" -Expected "200" -Actual $healthResponse.StatusCode -Note $healthResponse.RawBody

Write-Step "Seed account login"
$student1Login = Invoke-Api -Method "POST" -Path "/api/auth/login" -Body @{
    email = "student1@example.com"
    password = "Password123!"
}
Add-Result -Scenario "Login student1" -Expected "200" -Actual $student1Login.StatusCode -Note $student1Login.RawBody

$student2Login = Invoke-Api -Method "POST" -Path "/api/auth/login" -Body @{
    email = "student2@example.com"
    password = "Password123!"
}
Add-Result -Scenario "Login student2" -Expected "200" -Actual $student2Login.StatusCode -Note $student2Login.RawBody

$adminLogin = Invoke-Api -Method "POST" -Path "/api/auth/login" -Body @{
    email = "admin@example.com"
    password = "Admin123!"
}
Add-Result -Scenario "Login admin" -Expected "200" -Actual $adminLogin.StatusCode -Note $adminLogin.RawBody

$student1Headers = @{ Authorization = "Bearer $($student1Login.Body.accessToken)" }
$student2Headers = @{ Authorization = "Bearer $($student2Login.Body.accessToken)" }
$adminHeaders = @{ Authorization = "Bearer $($adminLogin.Body.accessToken)" }

Write-Step "BOLA / IDOR protection"
$student1Certificates = Invoke-Api -Method "GET" -Path "/api/certificates/me" -Headers $student1Headers
Add-Result -Scenario "Student1 list own certificates" -Expected "200" -Actual $student1Certificates.StatusCode -Note $student1Certificates.RawBody

$student1CertificateId = [string]$student1Certificates.Body[0].id
$bolaResponse = Invoke-Api -Method "GET" -Path "/api/certificates/$student1CertificateId" -Headers $student2Headers
Add-Result -Scenario "Student2 cannot read student1 certificate" -Expected "403" -Actual $bolaResponse.StatusCode -Note $bolaResponse.RawBody

Write-Step "Register demo student"
$suffix = Get-Date -Format "yyyyMMddHHmmssfff"
$demoEmail = "demo.crypto.$suffix@example.com"
$demoPassword = "Password123!"
$demoRegister = Invoke-Api -Method "POST" -Path "/api/auth/register" -Body @{
    fullName = "Demo Crypto Student $suffix"
    email = $demoEmail
    password = $demoPassword
    phoneNumber = "0909555$suffix".Substring(0, 10)
    billingAddress = "123 Demo Security Street"
}
Add-Result -Scenario "Register demo student" -Expected "201" -Actual $demoRegister.StatusCode -Note $demoRegister.RawBody
Add-Result -Scenario "Register response hides password hash" -Expected "False" -Actual ($demoRegister.RawBody.Contains("passwordHash")) -Note "API response should never leak passwordHash."

$demoHeaders = @{ Authorization = "Bearer $($demoRegister.Body.accessToken)" }

Write-Step "bcrypt and AES at rest"
$userRow = (Invoke-DbQuery "SELECT password_hash, phone_number_encrypted, billing_address_encrypted FROM users WHERE email = '$demoEmail';")[0]
$userColumns = $userRow -split "`t", 3
$passwordHash = $userColumns[0].Trim()
$phoneCipher = $userColumns[1].Trim()
$billingCipher = $userColumns[2].Trim()

Add-Result -Scenario "Password stored as bcrypt hash" -Expected "True" -Actual ($passwordHash.StartsWith("`$2")) -Note $passwordHash
Add-Result -Scenario "Phone stored encrypted" -Expected "True" -Actual ($phoneCipher -ne "0909555$suffix".Substring(0, 10)) -Note $phoneCipher
Add-Result -Scenario "Billing address stored encrypted" -Expected "True" -Actual ($billingCipher -ne "123 Demo Security Street") -Note $billingCipher

Write-Step "Course access before enrollment"
$courseDetailBefore = Invoke-Api -Method "GET" -Path "/api/courses/3" -Headers $demoHeaders
Add-Result -Scenario "Demo student can view public course detail" -Expected "200" -Actual $courseDetailBefore.StatusCode -Note $courseDetailBefore.RawBody
Add-Result -Scenario "Course 3 initially locked" -Expected "False" -Actual $courseDetailBefore.Body.enrolled -Note $courseDetailBefore.RawBody

$lockedLessonId = [string]$courseDetailBefore.Body.lessons[0].id
$lockedLesson = Invoke-Api -Method "GET" -Path "/api/courses/3/lessons/$lockedLessonId" -Headers $demoHeaders
Add-Result -Scenario "Locked lesson access denied before approval" -Expected "403" -Actual $lockedLesson.StatusCode -Note $lockedLesson.RawBody

Write-Step "Enrollment request and admin approval"
$enrollmentRequest = Invoke-Api -Method "POST" -Path "/api/courses/3/enrollment-requests" -Headers $demoHeaders
Add-Result -Scenario "Enrollment request created" -Expected "200" -Actual $enrollmentRequest.StatusCode -Note $enrollmentRequest.RawBody
Add-Result -Scenario "Enrollment request starts as pending" -Expected "PENDING" -Actual $enrollmentRequest.Body.status -Note $enrollmentRequest.RawBody

$approvedEnrollment = Invoke-Api -Method "POST" -Path "/api/admin/enrollments/$($enrollmentRequest.Body.enrollmentId)/approve" -Headers $adminHeaders
Add-Result -Scenario "Admin approves enrollment request" -Expected "200" -Actual $approvedEnrollment.StatusCode -Note $approvedEnrollment.RawBody
Add-Result -Scenario "Approved enrollment becomes active" -Expected "ACTIVE" -Actual $approvedEnrollment.Body.status -Note $approvedEnrollment.RawBody

Write-Step "Course access after approval"
$lessonAfterApproval = Invoke-Api -Method "GET" -Path "/api/courses/3/lessons/$lockedLessonId" -Headers $demoHeaders
Add-Result -Scenario "Lesson unlocked after admin approval" -Expected "200" -Actual $lessonAfterApproval.StatusCode -Note $lessonAfterApproval.RawBody
Add-Result -Scenario "Lesson returns full content after enrollment" -Expected "True" -Actual (-not [string]::IsNullOrWhiteSpace($lessonAfterApproval.Body.content)) -Note $lessonAfterApproval.RawBody

Write-Step "JWT tampering and expiration"
$tamperedToken = $student1Login.Body.accessToken.Substring(0, $student1Login.Body.accessToken.Length - 1) + "a"
$tamperedResponse = Invoke-Api -Method "GET" -Path "/api/auth/me" -Headers @{ Authorization = "Bearer $tamperedToken" }
Add-Result -Scenario "Tampered JWT is rejected" -Expected "401" -Actual $tamperedResponse.StatusCode -Note $tamperedResponse.RawBody

$nowEpoch = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$expiredToken = New-SignedJwt -Secret $jwtSecret -Payload ([ordered]@{
    sub = [string]$student1Login.Body.user.id
    email = [string]$student1Login.Body.user.email
    role = [string]$student1Login.Body.role
    scope = [string]$student1Login.Body.scope
    token_type = "access"
    iat = $nowEpoch - 7200
    exp = $nowEpoch - 3600
})
$expiredResponse = Invoke-Api -Method "GET" -Path "/api/auth/me" -Headers @{ Authorization = "Bearer $expiredToken" }
Add-Result -Scenario "Expired JWT is rejected" -Expected "401" -Actual $expiredResponse.StatusCode -Note $expiredResponse.RawBody

Write-Step "Role check and rate limit"
$studentAdminResponse = Invoke-Api -Method "GET" -Path "/api/admin/audit-logs" -Headers $demoHeaders
Add-Result -Scenario "Student cannot access admin audit logs" -Expected "403" -Actual $studentAdminResponse.StatusCode -Note $studentAdminResponse.RawBody

$rateLimitEmail = "rate.limit.$suffix@example.com"
$lastRateLimitResponse = $null
for ($attempt = 1; $attempt -le 6; $attempt++) {
    $lastRateLimitResponse = Invoke-Api -Method "POST" -Path "/api/auth/login" -Body @{
        email = $rateLimitEmail
        password = "WrongPassword123!"
    }
}
Add-Result -Scenario "Repeated login failures hit rate limit" -Expected "429" -Actual $lastRateLimitResponse.StatusCode -Note $lastRateLimitResponse.RawBody

Write-Step "Admin audit log review"
$auditLogResponse = Invoke-Api -Method "GET" -Path "/api/admin/audit-logs" -Headers $adminHeaders
Add-Result -Scenario "Admin can read audit logs" -Expected "200" -Actual $auditLogResponse.StatusCode -Note $auditLogResponse.RawBody

$auditActions = @($auditLogResponse.Body | Select-Object -ExpandProperty action)
Add-Result -Scenario "Audit log contains ACCESS_DENIED" -Expected "True" -Actual ($auditActions -contains "ACCESS_DENIED") -Note ($auditActions -join ", ")
Add-Result -Scenario "Audit log contains TOKEN_REJECTED" -Expected "True" -Actual ($auditActions -contains "TOKEN_REJECTED") -Note ($auditActions -join ", ")
Add-Result -Scenario "Audit log contains ENROLLMENT_APPROVED" -Expected "True" -Actual ($auditActions -contains "ENROLLMENT_APPROVED") -Note ($auditActions -join ", ")
Add-Result -Scenario "Audit log contains RATE_LIMIT_EXCEEDED" -Expected "True" -Actual ($auditActions -contains "RATE_LIMIT_EXCEEDED") -Note ($auditActions -join ", ")

Write-Step "Database evidence"
$enrollmentRow = (Invoke-DbQuery "SELECT id, student_id, course_id, status, activated_at FROM enrollments WHERE id = $($enrollmentRequest.Body.enrollmentId);")[0]
$certificateRow = (Invoke-DbQuery "SELECT certificate_code_encrypted FROM certificates ORDER BY id LIMIT 1;")[0]
Write-Host "Demo user email: $demoEmail"
Write-Host "bcrypt password hash: $passwordHash"
Write-Host "Encrypted phone: $phoneCipher"
Write-Host "Encrypted billing address: $billingCipher"
Write-Host "Enrollment row after approval: $enrollmentRow"
Write-Host "Encrypted certificate code: $certificateRow"

Write-Step "Summary"
$script:results | Format-Table Scenario, Expected, Actual, Outcome -AutoSize
