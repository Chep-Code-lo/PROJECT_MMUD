import type { Metadata } from "next";
import Navbar from "@/components/Navbar";
import "./globals.css";

export const metadata: Metadata = {
  title: "Online Course Security Lab",
  description:
    "Frontend demo for JWT, bcrypt, AES-GCM, HMAC webhook verification, audit logging, and OWASP API testing.",
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
