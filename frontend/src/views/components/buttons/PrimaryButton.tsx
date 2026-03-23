import type { ButtonHTMLAttributes } from 'react'

type PrimaryButtonProps = {
  loading?: boolean
} & ButtonHTMLAttributes<HTMLButtonElement>

export function PrimaryButton({
  loading,
  disabled,
  className = '',
  type = 'submit',
  children,
  ...props
}: PrimaryButtonProps) {
  return (
    <button
      type={type}
      disabled={disabled ?? loading}
      className={`flex w-full items-center justify-center rounded-lg bg-emerald-800 px-4 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-900 disabled:cursor-not-allowed disabled:opacity-60 ${className}`}
      {...props}
    >
      {loading ? (
        <span className="inline-flex items-center gap-2">
          <span
            className="size-4 animate-spin rounded-full border-2 border-white/40 border-t-white"
            aria-hidden
          />
          Please wait
        </span>
      ) : (
        children
      )}
    </button>
  )
}
