import type { ReactNode } from 'react'

type AuthCardProps = {
  children: ReactNode
}

export function AuthCard({ children }: AuthCardProps) {
  return (
    <div className="w-full max-w-2xl rounded-xl bg-white px-8 pb-8 pt-5 shadow-md shadow-gray-200/80 md:px-10 md:pb-10 md:pt-6">
      {children}
    </div>
  )
}
