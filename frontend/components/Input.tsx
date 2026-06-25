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
        <label className="mb-2 block text-sm font-semibold uppercase tracking-[0.18em] text-[#5b675f]">
          {label}
        </label>
      )}

      <input
        className={`w-full rounded-2xl border border-[#1f2a24]/12 bg-white/85 px-4 py-3 text-[#1f2a24] outline-none transition placeholder:text-[#8d958f] focus:border-[#0f766e] focus:ring-4 focus:ring-[#0f766e]/10 ${className}`}
        {...props}
      />

      {error && <p className="mt-2 text-sm text-[#b45309]">{error}</p>}
    </div>
  );
}
