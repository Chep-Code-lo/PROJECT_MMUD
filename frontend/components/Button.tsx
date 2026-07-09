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
    "inline-flex min-h-12 items-center justify-center rounded-full px-6 py-3 text-base font-bold tracking-wide transition duration-300 hover:-translate-y-0.5 active:translate-y-0 disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:translate-y-0";

  const variantClass = {
    primary:
      "bg-[#0f766e] text-[#f9f7f1] shadow-[0_16px_34px_rgba(15,118,110,0.28)] hover:bg-[#115e59] hover:shadow-[0_20px_42px_rgba(15,118,110,0.34)]",
    danger:
      "bg-[#b45309] text-[#fff8ef] shadow-[0_16px_34px_rgba(180,83,9,0.22)] hover:bg-[#92400e] hover:shadow-[0_20px_42px_rgba(180,83,9,0.28)]",
    secondary:
      "border border-[#1f2a24]/15 bg-white/88 text-[#12372f] shadow-[0_12px_26px_rgba(31,42,36,0.08)] hover:bg-[#f7efe3] hover:border-[#8b5e34]/30",
    success:
      "bg-[#14532d] text-[#f5fbf7] shadow-[0_16px_34px_rgba(20,83,45,0.22)] hover:bg-[#166534] hover:shadow-[0_20px_42px_rgba(20,83,45,0.28)]",
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
