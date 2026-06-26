import Link from "next/link";

export default function HomePage() {
  return (
    <main className="relative -mx-6 -my-8 flex min-h-screen items-center justify-center overflow-hidden px-6 py-12 md:-mx-10">
      <div className="absolute left-[8%] top-[14%] h-32 w-32 rounded-full border border-[#0f766e]/20 bg-[#0f766e]/10 blur-sm animate-float-soft" />
      <div className="absolute bottom-[12%] right-[10%] h-40 w-40 rounded-[42px] border border-[#b45309]/18 bg-[#b45309]/10 blur-sm animate-pulse-glow" />
      <div className="absolute right-[18%] top-[18%] h-24 w-24 rotate-12 rounded-[30px] border border-[#12372f]/10 bg-white/35 shadow-[0_24px_80px_rgba(18,55,47,0.12)] animate-float-soft" />

      <section className="relative z-10 flex w-full max-w-5xl flex-col items-center text-center animate-fade-in-up">
        <h1 className="max-w-4xl text-4xl font-bold leading-tight text-[#12372f] drop-shadow-sm md:text-6xl">
          Cloud API-Based Network Application Security for Small Company Services
        </h1>

        <div className="mt-10 flex w-full max-w-md flex-col gap-4 sm:flex-row sm:justify-center">
          <Link
            href="/register"
            className="inline-flex min-h-14 items-center justify-center rounded-full bg-[#0f766e] px-8 py-4 text-xl font-bold text-[#f7faf8] shadow-[0_20px_46px_rgba(15,118,110,0.32)] transition duration-300 hover:-translate-y-1 hover:bg-[#115e59] hover:shadow-[0_24px_56px_rgba(15,118,110,0.38)]"
          >
            Đăng ký ngay
          </Link>
          <Link
            href="/login"
            className="inline-flex min-h-14 items-center justify-center rounded-full border border-[#8b5e34]/30 bg-white/88 px-8 py-4 text-xl font-bold text-[#8b5e34] shadow-[0_18px_42px_rgba(31,42,36,0.12)] transition duration-300 hover:-translate-y-1 hover:bg-[#fff8ef] hover:text-[#6f4725]"
          >
            Đăng nhập
          </Link>
        </div>
      </section>
    </main>
  );
}
