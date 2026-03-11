import { UPDATE_OPERATO_LOGIS_BASE_URL } from '../actions/main'

const INITIAL_STATE = {
  operatoLogis: {
    baseUrl: null
  }
}

const operatoLogis = (state = INITIAL_STATE, action) => {
  switch (action.type) {
    case UPDATE_OPERATO_LOGIS_BASE_URL:
      let newState = JSON.parse(JSON.stringify(state))
      newState.operatoLogis.baseUrl = action.baseUrl
      return newState

    default:
      return state
  }
}

export default operatoLogis