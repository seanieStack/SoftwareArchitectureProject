import type { ReactNode } from 'react'

type AuthCardProps = {
  children: ReactNode
}

export function AuthCard({ children }: AuthCardProps) {
  return (
    <div className="w-full max-w-2xl rounded-xl bg-white p-8 shadow-md shadow-gray-200/80 md:p-10">
      {children}
    </div>
  )
}
