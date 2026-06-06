import katex from 'katex'

export function renderContentWithMath(htmlContainer: string): string {
  const parts = htmlContainer.split(/(\$[^$]+\$)/g)
  return parts
    .map((part) => {
      if (part.startsWith('$') && part.endsWith('$')) {
        const tex = part.slice(1, -1)
        try {
          return katex.renderToString(tex, { throwOnError: false, displayMode: false })
        } catch {
          return part
        }
      }
      return escapeHtml(part)
    })
    .join('')
}

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br/>')
}
