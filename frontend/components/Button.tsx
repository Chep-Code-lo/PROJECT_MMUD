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
    "px-4 py-2 rounded font-medium transition disabled:opacity-50 disabled:cursor-not-allowed";

  const variantClass = {
    primary: "bg-blue-600 text-white hover:bg-blue-700",
    danger: "bg-red-600 text-white hover:bg-red-700",
    secondary: "bg-gray-200 text-gray-900 hover:bg-gray-300",
    success: "bg-green-600 text-white hover:bg-green-700",
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