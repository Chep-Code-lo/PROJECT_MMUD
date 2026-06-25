import Link from "next/link";

const highlights = [
  {
    title: "Khoa hoc gon gang",
    text: "Noi dung duoc sap xep ngan gon, de theo doi va phu hop cho viec hoc online moi ngay.",
  },
  {
    title: "Giang vien dong hanh",
    text: "Moi khoa hoc deu co mo ta ro rang, bai hoc tung buoc va muc tieu cu the.",
  },
  {
    title: "Theo doi tien do",
    text: "Hoc vien co the xem khoa hoc da dang ky, ket qua va chung chi ngay trong tai khoan.",
  },
  {
    title: "Quan tri tap trung",
    text: "He thong co khu vuc rieng cho quan tri vien de theo doi nhat ky va nguoi dung.",
  },
];

const learningTopics = [
  "Java Security Basics",
  "Applied Cryptography for Beginners",
  "Secure RESTful API with Spring Boot",
];

export default function HomePage() {
  return (
    <main className="space-y-8">
      <section className="overflow-hidden rounded-[36px] border border-[#1f2a24]/10 bg-white/80 shadow-[0_28px_60px_rgba(31,42,36,0.08)]">
        <div className="grid gap-8 px-7 py-10 md:grid-cols-[1.2fr_0.8fr] md:px-10 md:py-12">
          <div className="space-y-5">
            <p className="text-xs font-semibold uppercase tracking-[0.36em] text-[#8b5e34]">
              Nen tang hoc truc tuyen
            </p>
            <h1 className="max-w-3xl text-4xl font-bold leading-tight text-[#12372f] md:text-5xl">
              Hoc online gon gang, de dang bat dau va theo doi tien do
            </h1>
            <p className="max-w-2xl text-base leading-8 text-[#4e5a53]">
              CourseHub tap trung vao cac chuc nang can thiet: dang ky, dang nhap,
              xem danh sach khoa hoc, hoc bai hoc da duoc mo khoa va theo doi ket
              qua hoc tap ngay tren tai khoan cua ban.
            </p>

            <div className="flex flex-wrap gap-3">
              <Link
                href="/courses"
                className="rounded-full bg-[#0f766e] px-5 py-3 text-sm font-semibold text-[#f7faf8] shadow-[0_18px_34px_rgba(15,118,110,0.22)]"
              >
                Kham pha khoa hoc
              </Link>
              <Link
                href="/login"
                className="rounded-full border border-[#1f2a24]/12 bg-white/70 px-5 py-3 text-sm font-semibold"
              >
                Dang nhap
              </Link>
            </div>
          </div>

          <div className="rounded-[28px] border border-[#1f2a24]/8 bg-[#f5efe4] p-6">
            <p className="mb-3 text-xs font-semibold uppercase tracking-[0.28em] text-[#6b705d]">
              Chu de noi bat
            </p>
            <div className="space-y-3 text-sm leading-7 text-[#33433c]">
              {learningTopics.map((topic) => (
                <div
                  key={topic}
                  className="rounded-2xl border border-white/70 bg-white/75 px-4 py-3"
                >
                  {topic}
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {highlights.map((feature) => (
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
