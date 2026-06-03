import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Security Dashboard",
  description: "Small company cloud API security dashboard"
};

export default function RootLayout({
  children
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}

