export function formatDateTime(dateValue: string | Date): string {
  if (!dateValue) return ''

  const date = new Date(dateValue)

  if (isNaN(date.getTime())) {
    return 'Data inválida'
  }

  const formattedDate = date.toLocaleDateString('pt-BR')
  const formattedTime = date.toLocaleTimeString('pt-BR', {
    hour: '2-digit',
    minute: '2-digit',
  })

  return `${formattedDate} ${formattedTime}`
}
