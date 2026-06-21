# Capstone Project — Cloud API‑Based Network Application Security for Small Company Services

**Môn:** NT219 - Cryptography

**Tiêu đề đề tài:** Bảo mật Ứng dụng Mạng Dựa trên API trên Cloud cho Dịch vụ Công ty Nhỏ — thiết kế, triển khai, và đánh giá các biện pháp bảo vệ API/Network phù hợp với tổ chức nhỏ

---

## 1. Tóm tắt đề tài (Project Synopsis)

Đề tài hướng dẫn sinh viên thiết kế, triển khai và đánh giá một **một hệ thống API‑first cho dịch vụ công ty nhỏ** (ví dụ: SaaS B2B nhỏ, e‑commerce microservice, or internal admin API), tập trung vào:

* Bảo vệ mặt phẳng API (authentication, authorization, token management),
* Bảo vệ luồng mạng (TLS/mTLS, service‑to‑service auth, network segmentation),
* Giải pháp tiết kiệm chi phí, dễ vận hành cho SME (managed services, serverless),
* Khả năng phát hiện & phản ứng (logging, tracing, alerting, SIEM nhẹ),
* Thực hành pentest & hardening (OWASP API Top 10, webhook security, signed requests).

Sinh viên sẽ tạo một prototype (Docker/Kubernetes or serverless), cấu hình API Gateway + IdP, áp dụng chính sách authz (RBAC/OPA), triển khai rate‑limiting/WAF, chạy bộ test bảo mật tự động, mô phỏng tấn công (replay, BOLA, SSRF, token leak), và viết báo cáo đề xuất rollout cho công ty nhỏ.

---

## 2. Mục tiêu học thuật & kỹ năng (Learning Objectives)

1. Hiểu rõ mô hình an ninh API: authentication (OAuth2/OIDC, mTLS, API keys), authorization (RBAC/ABAC/OPA), và token lifecycle.
2. Triển khai API Gateway, xác thực client (Authorization Code + PKCE, Client Credentials), webhook signing, và request signing (JWS/HMAC).
3. Thiết lập network controls: VPC, subnet segmentation, security groups, egress filtering; áp dụng mTLS cho service‑to‑service.
4. Thực hành phát hiện xâm nhập và logging: structured logs, correlation id, distributed tracing, siem/lightweight alerting.
5. Đánh giá bảo mật (automated SAST/DAST, OWASP ZAP, API fuzzing), lập runbook xử lý sự cố và chi phí/operational trade‑offs cho SME.

---

## 3. Tính cấp thiết & động lực (Relevance)

* Các công ty nhỏ ngày càng phụ thuộc vào APIs (mobile apps, SPA, 3rd‑party integrators) và thường thiếu năng lực vận hành an ninh. Một incident (token leak, stolen API key, BOLA) có thể phá hủy uy tín và gây tổn thất nặng nề.
* Vì hạn chế nhân lực/chi phí, giải pháp phải ưu tiên managed services, automation, dễ triển khai và rõ ràng trong vận hành.

---

## 4. Câu hỏi nghiên cứu & giả thuyết (RQ & Hypotheses)

**RQ1:** Kiến trúc đơn giản nào (API Gateway + IdP + managed WAF + KMS) cân bằng tốt giữa an toàn, vận hành và chi phí cho công ty nhỏ?

**RQ2:** Những vectơ tấn công API phổ biến nhất cho SME (BOLA, token theft, webhook hijack, SSRF) có thể được phòng chống hiệu quả bằng policies, automation và testing không?

**Giả thuyết:** Một stack chuẩn gồm API Gateway (rate limit + WAF + JWT validation), IdP (OIDC), short‑lived tokens, HSM/KMS cho secrets và automated detection (alerts + basic anomaly rules) sẽ giảm >80% các rủi ro phổ thông cho SME với chi phí vận hành chấp nhận được.

---

## 5. Background (Tổng quan ngắn)

* **OAuth2/OIDC:** Authorization Code + PKCE cho public clients, Client Credentials cho backend; refresh tokens; token introspection.
* **JWT pitfalls:** long‑lived tokens, alg none, kid confusion, no revocation.
* **mTLS & mutual auth:** strong option for S2S in microservices or service providers.
* **API Gateway / Edge:** terminates TLS, enforces authN/authZ, rate limits, injects tracing headers, connects to WAF.
* **WAF/Rate Limiting:** stop mass scanning, reduce brute force and credential stuffing.

---

## 6. Literature & standards (hướng khảo sát)

* OWASP API Security Top 10 (BOLA, Broken Auth, Excessive Data Exposure, Rate Limiting)
* OAuth 2.0 / OIDC RFCs; best practices (token binding, PKCE, rotating refresh tokens)
* NIST guidance on secure web services, JSON Web Token (RFC 7519 / JWS 7515) guidance

> Yêu cầu: sinh viên trích dẫn ít nhất 5 nguồn chính thức/canonical (OWASP, RFCs, NIST) và 3 công cụ/stack mẫu (API Gateway, Keycloak/Okta, HashiCorp Vault).

---

## 7. Thành phần hệ thống & tài nguyên (System Components & Resources)

### 7.1. Thành phần chính (microservice & edge view)

* **Client (web/mobile):** SPA + mobile app; Authorization Code + PKCE; local secure storage (Keystore/Keychain).
* **API Gateway (edge):** AWS API Gateway / CloudFront, Cloudflare Workers + Access, Kong/Envoy/Egress; nhiệm vụ: TLS, JWT verification, rate limiting, WAF, logging.
* **Identity Provider (IdP):** Keycloak (self‑hosted) or Auth0/Okta; manage users, clients, scopes, refresh policies.
* **Backend Microservices:** Order, User, Billing, Admin APIs — all enforce authz; S2S via mTLS or short‑lived client credentials.
* **Key Management / Secrets:** AWS KMS / GCP KMS / HashiCorp Vault; HSM for high‑value keys if affordable.
* **Logging & Monitoring:** ELK/EFK or managed CloudWatch + Datadog or Sumo Logic; simple SIEM rules for anomalies.
* **CI/CD & Supply Chain:** GitHub Actions / GitLab + signed artifacts, dependency scanning (Snyk), secrets scanning.

### 7.2. Tài nguyên phần cứng & chi phí cho SME

* Cloud managed resources: serverless functions or small k8s (k3s) nodes; managed DB (RDS/Firebase) to cut ops.
* Budget considerations: prefer managed IdP & API Gateway to avoid ops cost; small HSM can be replaced by cloud KMS in early stage.

---

## 8. Kịch bản triển khai thực tế (Deployment Scenarios)

### 8.1. Single‑region startup (lowest ops)

* API Gateway (managed) + Lambda (serverless) or single small k8s cluster; IdP as managed service; KMS cloud; WAF managed. Cost optimized, low ops.

### 8.2. Small multi‑tenant SaaS

* Multi‑tenant data isolation patterns, per‑tenant keys (envelope encryption); mTLS between services; per‑tenant rate limits; OPA for per‑tenant policy enforcement.

### 8.3. Hybrid on‑prem + cloud integration

* On‑prem internal services talk to cloud APIs via VPN; use mutual TLS and token exchange; central IdP with federated SSO.

---

## 9. Phân tích rủi ro & weaknesses theo kịch bản (Attack Surface Analysis)

### 9.1. Broken Object Level Authorization (BOLA)

* **Mô tả:** attacker manipulates object id in API path/param to access others' resources.
* **Nguyên nhân:** thiếu server‑side ACL checks; relying on client‑side filtering.
* **Mitigation:** enforce per‑request server‑side authz, use stable opaque IDs, and tests (BOLA fuzzing).

### 9.2. Broken Authentication / Token theft

* **Mô tả:** long‑lived access/refresh tokens stolen (XSS, insecure storage), or leaked API keys.
* **Mitigation:** short‑lived tokens, PKCE, rotate refresh tokens, refresh token rotation & binding, httpOnly secure cookies for web, token revocation list & introspection, device binding.

### 9.3. Excessive Data Exposure

* **Mô tả:** APIs return more fields than necessary.
* **Mitigation:** schema driven responses (OpenAPI), fields filtering server‑side, use DTOs, automated contract tests.

### 9.4. Rate limiting & abuse

* **Mô tả:** brute force, account enumeration, scraping, credential stuffing.
* **Mitigation:** per‑IP and per‑user rate limits, CAPTCHA, progressive backoff, global and per‑tenant quotas.

### 9.5. Webhooks & Third‑party callbacks

* **Mô tả:** webhook endpoints used by 3rd party; attacker forges callbacks.
* **Mitigation:** signed webhooks (HMAC with shared secret), replay nonce + timestamp, IP allowlist, strict validation.

### 9.6. SSRF / Open Redirect / Injection

* **Mô tả:** SSRF via URL fetch endpoints, metadata service access in cloud.
* **Mitigation:** block outbound to metadata/169.254.169.254, strict URL validation, allowlist hosts, network egress control.

### 9.7. Supply chain & CI/CD risks

* **Mô tả:** secrets in repos, compromised dependencies, malicious third‑party libs.
* **Mitigation:** secrets scanning, SCA tools, artifact signing, minimal third‑party use, SBOM.

---

## 10. Methodology (Pipeline & PoC Experiments)

Sinh viên nên thực hành một bộ thí nghiệm reproducible. Gợi ý pipeline:

1. **Stack setup:** API Gateway (Kong/Envoy) or AWS API Gateway + Lambda / small k8s cluster; IdP (Keycloak or Auth0 free tier); HashiCorp Vault or AWS KMS.
2. **Implement APIs:** User, Resource (CRUD), Admin endpoints with server‑side authN/authZ; use OpenAPI schema.
3. **Harden edge:** configure WAF rules, rate limits, CORS safe config, TLS 1.2+/1.3, HSTS.
4. **S2S security:** implement mTLS or short‑lived client credentials via IdP; use mutual TLS for internal services or JWT with short lifetime.
5. **Logging & detection:** structured logs (JSON), correlation IDs, export to ELK or cloud logs; add simple anomaly rules (spike in failed auths, new IPs, sudden rate).
6. **Automated testing:** run SAST (Bandit/Brakeman/ESLint), DAST (OWASP ZAP), API fuzzing (fuzzapi, restler), and BOLA test suites.
7. **Attack emulation:** simulate BOLA, token replay, SSRF, webhook forgery, and observe detection & mitigation.

---

## 11. Implementation & Tools (gợi ý tiết kiệm chi phí)

* **API Gateway / Edge:** Kong (open source), Envoy, AWS API Gateway (managed), Cloudflare Workers & Access.
* **IdP:** Keycloak (self‑hosted), Auth0/Okta (managed).
* **Secrets & KMS:** HashiCorp Vault (dev/OSS) or AWS KMS/Secrets Manager.
* **WAF:** Cloudflare WAF (managed), AWS WAF, ModSecurity.
* **Auth libs:** oidc‑client, oauth2‑sdk, jose/jwt libs (use well‑maintained libs).
* **Testing:** OWASP ZAP, Postman/Newman, restler, fuzzapi, Burp for manual pentest.
* **Logging / SIEM:** ELK stack, Grafana Loki, or managed CloudWatch + GuardDuty.
* **Policy engine:** OPA (Open Policy Agent) for authorisation decisions.

---

## 12. Evaluation Plan & Metrics

* **Security:** number of detected/blocked attack attempts in lab, % of OWASP API Top 10 tests mitigated.
* **Performance & cost:** added latency at edge (median/p95) from Gateway/WAF, KMS call overhead, monthly cost estimate for managed services.
* **Operational:** mean time to detect (MTTD) for simulated compromises, mean time to remediate (MTTR), frequency of false positives in alert rules.

---

## 13. Timeline & Milestones (12 tuần)

* **Tuần 1–2:** Requirement & threat model, choose stack (cloud vs self‑hosted), provision infra.
* **Tuần 3–4:** Implement core APIs, IdP integration (Auth code + PKCE), OpenAPI schema.
* **Tuần 5–6:** Configure API Gateway & WAF, implement rate limits, webhook signing, and mTLS for S2S.
* **Tuần 7:** Implement logging/tracing & simple detection rules.
* **Tuần 8:** Setup CI/CD with SAST and dependency scanning; add artifact signing.
* **Tuần 9:** Run DAST + fuzzing and fix findings.
* **Tuần 10:** Attack simulation (BOLA, token theft, SSRF, webhook forgery) and measure defenses.
* **Tuần 11:** Run resilience drills: key rotation, token revocation, incident response.
* **Tuần 12:** Finalize report, reproducible repo (Terraform/Helm/Docker), slides & demo video.

---

## 14. Deliverables

1. **Mid‑term:** architecture diagram, threat model, skeleton repo.
2. **Final report & repo:** reproducible infra (IaC), API code, test scripts, DAST/ZAP reports, logs, and remediation notes.
3. **Demo:** recorded live demo of secure flows and attack emulation.
4. **Runbooks:** incident response, key rotation, onboarding new client (BFF) patterns.

---

## 15. Assessment & Rubric (gợi ý)

* Architecture & threat modeling: 20%
* Implementation & reproducibility (IaC, automation): 30%
* Security testing & remediation (DAST, fuzzing, pentest): 30%
* Documentation, runbooks & presentation: 20%

---

## 16. Risks, Limitations & Ethical Considerations

* **Pen‑testing ethics:** only test lab infra and consented targets; do not run scans against third‑party services.
* **Managed services cost:** document cost estimates; don't use production secrets in demos.
* **Privacy:** use synthetic data for tests; sanitize logs before sharing.

---

## 17. Mitigations & Best Practices (summary recommendations)

* **Use strong authN/AuthZ:** OAuth2 + OIDC (PKCE), short‑lived access tokens, refresh token rotation, OPA for fine‑grained policies.
* **Harden edge:** TLS 1.3, strict CORS, WAF, rate limiting, signed webhooks, HMAC validation.
* **Service‑to‑service security:** mTLS or short‑lived client credentials + certificate rotation.
* **Secrets & keys:** centralize in KMS/Vault, enforce least privilege, automate rotation.
* **Observability:** structured logging, distributed tracing, anomaly alerts, periodic security scans.
* **CI/CD & supply chain:** SAST, SCA, artifact signing, SBOM, minimal runtime dependencies.

---

## 18. Extensions & Future Work

* Add ML‑based anomaly detection for API abuse; integrate with SOAR for automated response.
* Evaluate PoP tokens (DPoP/mTLS bound tokens) and token binding to reduce replay.
* Integrate with WAF rule tuning based on ML and real traffic.

---

## 19. Tools & Resources gợi ý

* Keycloak, Auth0, Okta, Kong/Envoy, AWS API Gateway, Cloudflare Access, HashiCorp Vault, OPA, OWASP ZAP, Burp Suite, restler/fuzzapi, ELK/CloudWatch/Grafana.

---

## 20. Appendix: Repository Structure (mẫu)

```
project-root/
  ├─ infra/              # Terraform / Helm charts for API Gateway, IdP, k8s
  ├─ services/           # user/, resource/, admin/ microservices
  ├─ gateway/            # Kong/Envoy configs, WAF rules
  ├─ idp/                # Keycloak realm exports, client configs
  ├─ tests/              # zap/, restler/, fuzzapi scripts, unit tests
  ├─ ci/                 # SAST, SCA, artifact signing workflows
  ├─ docs/               # runbooks, threat model, final report
  └─ demo/               # scripts to replay demo scenarios + dataset (synthetic)
```

---

*Ghi chú cho sinh viên:* nêu rõ mọi phiên bản thư viện, provider, commit hashes, thiết lập IaC, và phân biệt rõ phần nào là simulation so với thử nghiệm trên môi trường production. Luôn tuân thủ đạo đức và pháp luật khi thực hiện pentest/attack simulations.

---