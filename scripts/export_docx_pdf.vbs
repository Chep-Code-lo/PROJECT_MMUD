Option Explicit

If WScript.Arguments.Count < 2 Then
    WScript.Echo "Usage: cscript //nologo export_docx_pdf.vbs input.docx output.pdf"
    WScript.Quit 1
End If

Dim fso
Dim inputPath
Dim outputPath
Dim wordApp
Dim doc

Set fso = CreateObject("Scripting.FileSystemObject")
inputPath = fso.GetAbsolutePathName(WScript.Arguments(0))
outputPath = fso.GetAbsolutePathName(WScript.Arguments(1))

Set wordApp = CreateObject("Word.Application")
wordApp.Visible = False
wordApp.DisplayAlerts = 0

Set doc = wordApp.Documents.Open(inputPath, False, True, False)
doc.ExportAsFixedFormat outputPath, 17
doc.Close False
wordApp.Quit
