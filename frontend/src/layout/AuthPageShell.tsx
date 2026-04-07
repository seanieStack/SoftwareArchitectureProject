import type { ReactNode } from 'react'
import { AuthNavbar } from './AuthNavbar'

type AuthPageShellProps = {
  children: ReactNode
}

export function AuthPageShell({ children }: AuthPageShellProps) {
  return (
    <div className="flex min-h-screen flex-col bg-gray-50">
      <AuthNavbar />
      <main className="flex flex-1 flex-col items-center px-4 py-10 md:py-14">
        {children}
      </main>
    </div>
  )
}
