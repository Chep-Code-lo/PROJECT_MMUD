import type { Metadata } from "next";
import Navbar from "@/components/Navbar";
import "./globals.css";

export const metadata: Metadata = {
  title: "Security Course Lab",
  description: "Mo hinh demo RESTful API an toan cho do an Mat ma ung dung.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="vi">
      <body>
        <Navbar />
        <div className="mx-auto max-w-6xl px-6 py-8 md:px-10">{children}</div>
      </body>
    </html>
  );
}
