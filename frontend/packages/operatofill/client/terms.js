import { operatoGetData, currentLocale } from './operatofill.js'

export class OperatoTerms {
  /**
   * 용어 : 용어 키 - 용어 표현 값 
   */
  OPERATO_TERMS = {}

  /**
   * Operato 서버에 현재 로케일에 맞는 용어를 모두 조회
   ***********************************************
   * @returns 
   */
  static async operatoAllTerminologies() {
    let locale = currentLocale()
    let url = `terminologies/resource/${locale}.json`
    let data = await operatoGetData(url)
    this.OPERATO_TERMS = data[locale]
  }

  /**
   * termKey로 용어 변환하여 리턴
   ***********************************************
   * @param {String} termKey 
   */
  static t1(termKey) {
    if (this.OPERATO_TERMS) {
      return this.OPERATO_TERMS[termKey]
    } else {
      return termKey
    }
  }

  /**
   * category, name으로 termKey를 구성하여 용어 변환하여 리턴
   ***********************************************
   * @param {String} category
   * @param {String} name
   */
  static t2(category, name) {
    return OperatoTerms.t1(`${category}.${name}`)
  }

  /**
   * termKey로 용어 변환하고 변수를 parameters에서 찾아서 치환하여 리턴
   ***********************************************
   * @param {String} termKey
   * @param {Object} parameters
   */
  static t3(termKey, parameters) {
    let term = OperatoTerms.t1(termKey)

    if (term) {
      Object.keys(parameters).forEach(function(param) {
        var bracedData = '\\' + '{' + param.replace(/\$/, '\\$') + '\\' + '}';
        var regEx = new RegExp(bracedData, "gi");
        term = term.replace(regEx, parameters[param]);
      });
      return term

    } else {
      return termKey
    }
  }

  /**
   * category, name으로 termKey를 구성하여 용어 변환하고 변수를 parameters에서 찾아서 치환하여 리턴
   ***********************************************
   * @param {String} category
   * @param {String} name
   * @param {Object} parameters
   */
  static t4(category, name, parameters) {
    return OperatoTerms.t3(`${category}.${name}`, parameters)
  }
}