export default function route(page) {
  switch (page) {
    case '':
      return '/metapage-main'

    case 'metapage-main':
      import('./pages/main')
      return page
  }
}
