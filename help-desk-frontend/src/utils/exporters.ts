export function exportToCsv(
  filename: string,
  data: { [key: string]: number },
  headers: [string, string],
) {
  let csvContent = `"${headers[0]}","${headers[1]}"\n`

  for (const key in data) {
    if (Object.prototype.hasOwnProperty.call(data, key)) {
      csvContent += `"${key}",${data[key]}\n`
    }
  }

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  if (link.download !== undefined) {
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `${filename}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }
}
