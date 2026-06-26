import type { InputHTMLAttributes } from "react";

type InputProps = InputHTMLAttributes<HTMLInputElement> & {
  label?: string;
  error?: string;
};

export default function Input({
  label,
  error,
  className = "",
  ...props
}: InputProps) {
  return (
    <div className="w-full">
      {label && (
        <label className="mb-2 block text-base font-bold text-[#3f4f47]">
          {label}
        </label>
      )}

      <input
        className={`w-full rounded-2xl border border-[#1f2a24]/14 bg-white/90 px-5 py-3.5 text-lg text-[#1f2a24] shadow-inner shadow-[#1f2a24]/[0.03] outline-none transition duration-300 placeholder:text-[#8d958f] focus:border-[#0f766e] focus:bg-white focus:ring-4 focus:ring-[#0f766e]/12 ${className}`}
        {...props}
      />

      {error && <p className="mt-2 text-base text-[#b45309]">{error}</p>}
    </div>
  );
}
