  // Operato Logis Base URL 설정을 위한 상수
  export const UPDATE_OPERATO_LOGIS_BASE_URL = 'UPDATE_OPERATO_LOGIS_BASE_URL'

  export const updateOperatoLogisBaseUrl = baseUrl => (dispatch, getState) => {
    dispatch({
      type: UPDATE_OPERATO_LOGIS_BASE_URL,
      baseUrl
    })
  }