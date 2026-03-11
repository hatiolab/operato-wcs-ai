# Data Binding

## Concept

- Every component has its own data attribute.
- The data property of the component can be set as follows.
  - Initial Data: Initial data can be set by the modeler during modeling.
  - In case of data source component: Set the result received from the data source as own data.
  - For UI-based components: The value changed by the user's interaction is set as data.
  - Data Spread: It can be set by setting the Data Binding of other components.
- The properties of the target component(s) can be changed according to the data binding model set in response to changes in component data.
- PUBSUB (Publish-Subscribe)
  - The method by which a data publisher sends tagged data to multiple data subscribers is called pubsub.
  - The server that provides the board service acts as a data publisher, and users can publish data through the publish task of the scenario.
  - Each component can designate its own tag property, and among the data transmitted by the publisher, data corresponding to the specified tag is received.
  - The received message is immediately stored in data and causes data propagation.

## Identifier

### ID

-Unique name of component

### class

- Class of component
- Multiple classifications separated by spaces can be set
- Each class can be designated as a target when transmitting data.

### tag

- Designates the tag of the data to be received among the data transmitted from the data publisher.

### template prefix

- Can be set when designating a component as a template.
- Components set as a template are not displayed in the viewer.
- Referred to when dynamically creating a component using a template by cloning.

### no data no show

- When operating in board viewer mode, it is a setting that is not displayed when data is not set in the component.

### intent sensitive

- Data propagation works when the data of the source component is changed. In other words, even if the data is newly set, data propagation does not work unless the value actually changes. This is a method to increase the efficiency of monitoring purposes.
- However, when the intent sensitive setting is turned on, data propagation works even if the data has not changed, even if the data of the source component is newly set, even if the value has not actually changed.
- This property is also applied to the operation according to the setting of the value property of the source component.

## Initial Data

- Set the initial data of the component.
- Data can be set to one of string, number, or JSON object type.

## Data Spread

- This setting changes the properties of the target component based on the data value of the source component.
- If the source component changes the data property of the target component, the Data Spread function of the target component operates continuously, so this function is called data propagation.
- Multiple data propagation settings are possible

### accessor

- Set when you want to propagate only a part of the data object of the source component.
- Property name of data object can be specified.
- If the data object's property is deep, it can be specified continuously by separating it with'.'.

### target

- Designate the target component for data propagation.
- Designated by id: #id
- Designated as class: .class
- Target designation based on data
  - (key): Components with the key value of the data object as the id become target components.
  - [property]: Components that have the property value of the data object as the id become the target component.

### property

- Designates the properties of the target component to which the data value to be propagated is applied.

### rule

Specifies how the data to be propagated is applied to the target component properties.

- value
  - the value itself is set as the target attribute of the component.
- map
  - mapping the value of the key value is set in the properties of the target component.
- range
  - The mapping value of the range including the value is set in the properties of the target component.
- eval
  - The eval code can be written in JavaScript grammar.
  - The context (this) of the eval code is a source component.
  - The parameter of eval code has value and targets.
    - value: The value to which the accessor is applied to the data of the source component
    - targets: list of target components

### No Data No Spreading

- Data will be spreaded only when the final result value according to the rule type is not an empty value.
  - If the result value is undefined, null, NaN, or blank (''), it will not be spreaded.
  - Otherwise, data will be spreaded, even if the result is the number zero.
