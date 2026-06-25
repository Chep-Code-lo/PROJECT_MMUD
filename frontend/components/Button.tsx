import type { ButtonHTMLAttributes, ReactNode } from "react";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  children: ReactNode;
  variant?: "primary" | "danger" | "secondary" | "success";
};

export default function Button({
  children,
  variant = "primary",
  className = "",
  ...props
}: ButtonProps) {
  const baseClass =
    "inline-flex items-center justify-center rounded-full px-4 py-2.5 text-sm font-semibold tracking-wide transition disabled:cursor-not-allowed disabled:opacity-50";

  const variantClass = {
    primary:
      "bg-[#0f766e] text-[#f9f7f1] shadow-[0_14px_30px_rgba(15,118,110,0.25)] hover:bg-[#115e59]",
    danger:
      "bg-[#b45309] text-[#fff8ef] shadow-[0_14px_30px_rgba(180,83,9,0.2)] hover:bg-[#92400e]",
    secondary:
      "border border-[#1f2a24]/15 bg-white/80 text-[#1f2a24] hover:bg-[#f3ede4]",
    success:
      "bg-[#14532d] text-[#f5fbf7] shadow-[0_14px_30px_rgba(20,83,45,0.2)] hover:bg-[#166534]",
  };

  return (
    <button
      className={`${baseClass} ${variantClass[variant]} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
}
