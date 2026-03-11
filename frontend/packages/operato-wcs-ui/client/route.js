export default function route(page) {
  switch (page) {
    case '':
      return '/operato-wcs-ui-main'

    case 'operato-wcs-ui-main':
      import ('./pages/main')
      return page
  }
}