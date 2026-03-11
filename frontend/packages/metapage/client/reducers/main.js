import { UPDATE_METAPAGE } from '../actions/main'

const INITIAL_STATE = {
  metapage: 'ABC'
}

const metapage = (state = INITIAL_STATE, action) => {
  switch (action.type) {
    case UPDATE_METAPAGE:
      return { ...state }

    default:
      return state
  }
}

export default metapage
