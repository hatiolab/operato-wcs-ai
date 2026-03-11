# Popup

This component displays the target board in a pop-up window.

## Properties

- board
  - Select the target board to be displayed in a popup.
- show
  - Set when you want to show a popup at the start time.
- started
  - Although not shown on the property screen, if this property is set to true/false during data binding, the popup will be shown or closed.
  - boolean type
- modal
  - Set whether the popup is modal.
- closable
  - Set whether to show the close button.
- location
  - Designate the position where the pop-up window will be displayed.
  - If set to auto, it will be displayed in the position with the most space opposite this Popup component.
- value
  - Designate the initial value of the target board to be displayed as a popup.
  - When the value value is set by data binding, the started property automatically becomes true and a popup is displayed.
  - If the value is reset while the popup is already displayed, the existing popup will be re-opened with the new initial value.
- data
  - When the popup is closed by clicking a user-defined button, the data of the corresponding button is set in the data of this popup component.
  - It is used in the meaning of processing the pop-up execution result.
