import Link from "next/link";

const features = [
  {
    title: "JWT + Refresh Token",
    text: "Client dang nhap, nhan Bearer token, goi API protected va test token tampered.",
  },
  {
    title: "bcrypt Password Hashing",
    text: "Mat khau khong luu plaintext. Backend luon hash bang BCryptPasswordEncoder.",
  },
  {
    title: "AES-GCM Data Protection",
    text: "Phone, billing address, certificate code va payment reference duoc ma hoa khi luu DB.",
  },
  {
    title: "HMAC Webhook + Audit Log",
    text: "Webhook thanh toan duoc ky HMAC-SHA256, chong replay va ghi audit log khi rejected.",
  },
];

const accounts = [
  "student1@example.com / Password123!",
  "student2@example.com / Password123!",
  "instructor@example.com / Password123!",
  "admin@example.com / Admin123!",
];

export default function HomePage() {
  return (
    <main className="space-y-8">
      <section className="overflow-hidden rounded-[36px] border border-[#1f2a24]/10 bg-white/80 shadow-[0_28px_60px_rgba(31,42,36,0.08)]">
        <div className="grid gap-8 px-7 py-10 md:grid-cols-[1.2fr_0.8fr] md:px-10 md:py-12">
          <div className="space-y-5">
            <p className="text-xs font-semibold uppercase tracking-[0.36em] text-[#8b5e34]">
              Do an Mat ma ung dung / Cryptography
            </p>
            <h1 className="max-w-3xl text-4xl font-bold leading-tight text-[#12372f] md:text-5xl">
              Bao mat he thong RESTful API cho dich vu khoa hoc online nho
            </h1>
            <p className="max-w-2xl text-base leading-8 text-[#4e5a53]">
              Frontend nay duoc giu toi gian de demo ro cac luong bao mat:
              dang nhap JWT, lesson locked/unlocked, certificate ownership,
              admin audit logs va webhook thanh toan ky HMAC.
            </p>

            <div className="flex flex-wrap gap-3">
              <Link
                href="/courses"
                className="rounded-full bg-[#0f766e] px-5 py-3 text-sm font-semibold text-[#f7faf8] shadow-[0_18px_34px_rgba(15,118,110,0.22)]"
              >
                Xem danh sach khoa hoc
              </Link>
              <Link
                href="/login"
                className="rounded-full border border-[#1f2a24]/12 bg-white/70 px-5 py-3 text-sm font-semibold"
              >
                Dang nhap de test API
              </Link>
              <a
                href="/swagger-ui.html"
                className="rounded-full border border-[#b45309]/20 bg-[#fff3e6] px-5 py-3 text-sm font-semibold text-[#9a3412]"
              >
                Swagger UI
              </a>
            </div>
          </div>

          <div className="rounded-[28px] border border-[#1f2a24]/8 bg-[#f5efe4] p-6">
            <p className="mb-3 text-xs font-semibold uppercase tracking-[0.28em] text-[#6b705d]">
              Tai khoan mau
            </p>
            <div className="space-y-3 text-sm leading-7 text-[#33433c]">
              {accounts.map((account) => (
                <div
                  key={account}
                  className="rounded-2xl border border-white/70 bg-white/75 px-4 py-3"
                >
                  {account}
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {features.map((feature) => (
          <article
            key={feature.title}
            className="rounded-[28px] border border-[#1f2a24]/8 bg-white/78 p-6 shadow-[0_18px_40px_rgba(31,42,36,0.06)]"
          >
            <h2 className="mb-3 text-xl font-semibold text-[#163d35]">
              {feature.title}
            </h2>
            <p className="text-sm leading-7 text-[#536059]">{feature.text}</p>
          </article>
        ))}
      </section>
    </main>
  );
}
