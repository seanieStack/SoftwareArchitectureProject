type AuthHeaderProps = {
  title: string
  subtitle: string
  /** When true, title and subtitle are centered (e.g. register). */
  align?: 'start' | 'center'
}

export function AuthHeader({ title, subtitle, align = 'start' }: AuthHeaderProps) {
  const alignClass = align === 'center' ? 'text-center' : 'text-left'
  return (
    <header className={`mb-8 space-y-2 ${alignClass}`}>
      <h1 className="text-2xl font-bold tracking-tight text-gray-900 md:text-3xl">
        {title}
      </h1>
      <p className="text-sm text-gray-500 md:text-base">{subtitle}</p>
    </header>
  )
}
