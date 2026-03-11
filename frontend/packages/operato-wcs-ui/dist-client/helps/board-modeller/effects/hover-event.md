# hover event

Defines the behavior when the mouse is hovered over the component.

## Description

### Emphasize

Creates the effect of highlighting the component borders.

### Action

- popup target board
  - The target board will be displayed in a pop-up window.
  - the position of the pop-up window is in view of the size and location of the components of the hover pop-up target board is shown in one of the following locations.
    - top-left, top-right, bottom-left, bottom-right, center
  - The size of the pop-up window should not be larger than the size of the current board. If the size of the pop-up target board is larger than the current board, it becomes difficult to see all the contents of the pop-up target board.
  - To close the pop-up window, press the'X' close button at the top right of the pop-up window, or if the tab event of a specific component of the target board is set to'close current board', you can click the component to close it.
  - Not affected by setting of 'restore on leave'
  - parameter
    - target : Specify the target board to show from list on the board.
- open infowindow
  - 'info-window' With the content set in the component, the info window is opened at a location close to the hovered point.
  - When the mouse point leaves the component, the info window is also closed with setting of 'restore on leave'
  - parameter
    - target : Specifies the ID of the target info window.
- toggle target component data
  - Toggles data in the order of Boolean value true/false based on the current data of the target component.
  - parameter
    - target : Target component to toggle data
- tristate(0/1/2) target component data
  - Based on the current data of the target component, data will be cycled in the order of number 0/1/2.
  - parameter
    - target : Target component to rotate data
- set value to target component data
  - Change the 'data' of the target component with the set value.
  - Usually, changing the data of a component has the effect of triggering a data spread assigned to the target component.
  - parameter
    - target : Target component to change data
    - value : Value to be set in the target component (string, number or object type can be specified)
- set value to target component value
  - Change the 'value' of the target component with the set value.
  - Usually, changing the value of a component has the effect of triggering a specified action of the target component.
  - parameter
    - target :  Target component to change value
    - value : Value to be set in the target component (string, number or object type can be specified)

### target
- The target board or component, target editor will be changed by selected action, it can be 'board list' or 'component drop down'
### restore on leave

When the mouse leaves the component, the following effect is a setting to cancel.

- 강조
- open infowindow
- toggle target component data
- tristate(0/1/2) target component data
