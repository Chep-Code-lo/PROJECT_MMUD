import Link from "next/link";

export default function HomePage() {
  return (
    <main className="relative flex min-h-screen items-center justify-center px-6">
      <div className="absolute right-6 top-6 flex items-center gap-3 md:right-10 md:top-8">
        <Link
          href="/login"
          className="rounded-full border border-[#1f2a24]/12 bg-white/85 px-4 py-2 text-sm font-medium text-[#12372f] transition hover:bg-white"
        >
          Đăng nhập
        </Link>
        <Link
          href="/register"
          className="rounded-full bg-[#0f766e] px-4 py-2 text-sm font-semibold text-[#f7faf8] shadow-[0_16px_30px_rgba(15,118,110,0.18)] transition hover:bg-[#115e59]"
        >
          Đăng ký
        </Link>
      </div>

      <h1 className="text-center text-2xl font-semibold text-[#12372f] md:text-4xl">
        Cloud API Based Network Application Security for Small Company Services
      </h1>
    </main>
  );
}
