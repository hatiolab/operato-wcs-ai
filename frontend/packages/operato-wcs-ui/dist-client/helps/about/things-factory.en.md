# about things factory

Things-Factory is an framework to develop web-based apps for desktop, mobile and edge device environment.

Things-Factory framework is managed as a monorepo that is composed of hundreds of npm packages.
These modules compose together to help you create performant modern JS apps that you love to develop and test. These packages are developed primarily to be used on top of the stack we like best for our JS apps; Typescript for the flavor, Koa for the server, LitElement for UI, Apollo for data fetching, and Jest for tests. That said, you can mix and match as you like.

- [Contents for modules](./modules.md)
- [Contents for components](./components.md)

## features

- It is based on module structure.
  - You can configure related feature sets for each module.
  - It is removable by module.
  - You can define dependencies between modules. Apply nodejs module dependency structure.
  - [Things Factory Modules](./modules.md)
  - [What modules does this application consist of ? (click here with holding ALT key.)](/dependencies)
- Responsible Web App
  - by Service Worker, Webpush notification, Manifest
  - application layout for mobile
- Help
  - markdown based online help
- Support Theme
- Monorepo managed by lerna and yarn
  - [things-factory github repository](https://github.com/hatiolab/things-factory)
- Powerful Graphic UI design & viewing tools (board modules)
- Redux pattern
- Use i18next for internationalization
- Simple datagrid
  - responsible design
- Packaging function uses webpack
- The code set is based on es6,7,8, and transpiling with babel
  - class-properties
  - decorators
  - object-rest-spread
- Using Web Component-based DomElement - Defining custom element with LitElement
- It is activated by linking with URL path and history in Single Page Application.
- Each page is loaded at the time of initial activation. (lazy loading)
- Zero Configuration function. It uses SSDP (Service Discovery Protocol) to automatically find services to be connected automatically and provide related functions. For example, it attempts to connect automatically by finding a server to connect to.
- JWT based authentication (requires improvement to be supported by the server framework)
- Oauth2 based extern interface
  - as a Oauth2 Server
  - as a Oauth2 Client
- Free branding
- Deploy
  - [docker](http://docker.com) ready
  - [kubernetes](http://kubernetes.io) ready

## structural conventions

- Redux
  - The way to manage the overall state of an Application is using Redux pattern.
  - The application, or base modules, can prepare points in the store that can be extended by a sub-module and provide an action.
  - The sub-modules connect to the extension structure of the application or parent module using the action defined in the parent (base) module.
- Page, Layout and Component
  - Pages are activated by associating with the URL. (route)
  - Layouts provide a framework for the layout and structure of the entire application UI elements.
  - The components provide independent (atomic) functionality.
    - The component excludes dependencies on the application in order to maintain implementation independence.
      - Style maintains its independence by using css variables.
      - Multilinguals maintain independence by using i18n-msg components. (Only have dependencies on i18n-msg.)
      - The component does not have a premise for the layout (display, position) of the host (container) to which it will be used.
  - page, layouts are connected to redux.
  - Components do not connect to redux, but they work with pages through properties.
- Base Module and extension (common) Modules (ex. provider, ui)
  - The Base Module provides an abstract definition of special functions.
  - The Base Module is said to provide functional services to the extension module.
  - The extension module can add the base module as a dependent module and directly use the functions defined in the base module.
  - Another way the extension module extends the Base Module is by using the reducer and action added to the store.
  - The Base Module is a base module that reads a group of modules. For example, board-base module is a base module for all board functions. In addition, label-base module defines and implements functions related to the label, including barcode label pop-up, barcode label scanning, barcode label rendering and printing.
  - The naming convention of base module for xxx module is 'xxx-base'.
- Provider Module
  - Sub-module that fills the data of Base module
  - It mainly serves to fill the data defined in Base module from the external server.
  - The naming convention for Provider module of xxx module is 'xxx-provider'.
  - If it is necessary to be distinguished by the way data is provided, the naming convention for Provider module of xxx module using yyy method can be defined as 'xxx-provider-yyy'.
- UI Module
  - Sub-module responsible for the screen configuration related to Base module
  - The naming convention for UI module of xxx module is 'xxx-ui'.
  - If it is necessary to be distinguished by UI configuration method, the naming convention for UI module of xxx module using yyy method may be 'xxx-ui-yyy'.
- Shell, Module and Application
  - Shell provides all the structures that enable the module structure in the development and execution stages.
    - redux, assets, routing
    - build module, build application
  - Module is only responsible for implementing its own purpose in the Shell base.
  - Application is the final product that consists of the modules necessary for the purpose of Shell and the user.
    - Configurations by Application
      - Manifest file (brand-related - application name, logo image)
      - Other brand related - homepage link, banner logo
      - Style : Representative color table, other css variables
      - Server connection IP
      - Other source/resource override

## Other coding conventions

- Source file length recommended
  - Each source file implements one of the most important purposes. (Be responsible for simple purposes rather than multiple purposes.)
  - If there is no specific reason for each source file, it is recommended not to exceed 200 lines.
- Naming

  - class
    - Class name : starts with uppercase, camel-case
    - private property, method : starts with \_(underscore), camel-case
    - public property, method : starts with lowercase, camel-case
    - class property, method : starts with lowercase, camel-case
    - Event handler : starts with on + EventName, camel-case

- Localization
  - Don’t capitalize explicitly in locale file

```
"field.system brief": "System Brief" (x)
"field.system brief": "system brief" (o)
```

You can set it to style where you need it.

```
text-transform: capitalize;
text-transform: uppercase;
```

- If possible, use ‘ms.json’ rather than ‘ms-My.json’. In particular, use ‘ms-My’ only if it is a Malay language that is used differently only in Malaysia.

## authentication

### things-factory/shell

- Control the authentication process through auth base.
  - Set authentication through auth base.
- Provide auth action.
  - You can change store through authentication related action.
- Provide auth reducer.
- That is, it sets through auth base and provides auth-related extension point.
- In particular, auth base function is provided directly in things-factory shell.

### things-factory/auth-ui

- Provide the client side authentication process based on JSON webtoken.

## prerequisites

- Install Windows Subsystem for Linux on Windows (Windows)
  - Install WSL using Ubuntu following https://docs.microsoft.com/en-gb/windows/wsl/install-win10
- VS Code Extensions
  - Prettier - Code formatter
  - es6-string-css
  - lit-html
- VS Code Configuration
  - Format on save: true
- nodejs (v8.0.0 and above)
- yarn
- Chrome Browser Extension
  - https://github.com/zalmoxisus/redux-devtools-extension
- Node-gyp (For Ubuntu)
  - npm install -g node-gyp
- Python2 (For Ubuntu)
  - sudo apt-get install python
  - npm install --python=python2.7
  - npm config set python python2.7
- mdns (For Ubuntu)
  - sudo apt-get install build-essential
  - sudo apt-get install libavahi-compat-libdnssd-dev
- node-printer (For Ubuntu)
  - sudo apt-get install libcups2-dev

## Usages

The things-factory repo is managed as a monorepo that is composed of hundreds of npm packages.
Each package has its own `README.md` and documentation describing usage.

```
# very first time
$ yarn install
$ yarn build # build all packages
$ DEBUG=things-factory:*,typeorm:* yarn workspace @things-factory/operato-mms run migration
$ DEBUG=things-factory:* yarn workspace @things-factory/operato-mms run serve:dev
```

```
# after a new module package(ie. @things-factory/newbee) added
$ yarn install # make newbee package join
$ yarn workspace @things-factory/newbee build
$ DEBUG=things-factory:* yarn workspace @things-factory/operato-mms run serve:dev
```

```
# after a dependent package(ie. @things-factory/dependa) modified
$ yarn workspace @things-factory/dependa build
$ DEBUG=things-factory:* yarn workspace @things-factory/operato-mms run serve:dev
```

```
# run application (ie. @things-factory/operato-mms) in production mode
$ yarn workspace @things-factory/dependa build
$ yarn workspace @things-factory/dependa build:client
$ yarn workspace @things-factory/operato-mms run serve

# The way to use the config file is the same as before.
# Don't forget to give a config file to make the app run.
```

```
# generate new application (ie. @things-factory/operato-xyz)
$ yarn generate app
  ? What should this application's name be? Ex. operato-abc > # type "operato-xyz"

# generate new module (ie. @things-factory/notification)
$ yarn generate module
  ? What should this module's name be? Ex. menu > # type "notification"

# generate new entity in a module (ie. "sms" entity in @things-factory/notification module)
$ yarn generate entity
  ? What is target package's name? Ex. biz-base, operato-mms > # type "notification"
  ? What should this entitie's name be? Ex. company, company-ext > # type "sms"

# generate new page in a module (ie. "sms-view" page in @things-factory/notification module)
$ yarn generate page
  ? What is target package's name? Ex. biz-base, operato-mms > # type "notification"
  ? What should this pages's name be? Ex. abc-viewer > # type "sms-view"

# generate new scene-module from scratch (ie. @things-factory/scene-random)
$ yarn generate app
  ? What should this module's name be? Ex. random > # type "scene-random"

# generate new scene-component in a module (ie. "button" component in @things-factory/scene-switch module)
$ yarn generate component
  ? What is target package's name? Ex. switch > # type "switch"
  ? What should this component's name be? Ex. button > # type "button"

# generate new container component in a module (ie. "button" container component in @things-factory/scene-switch module)
$ yarn generate container
  ? What is target package's name? Ex. switch > # type "switch"
  ? What should this component's name be? Ex. button > # type "button"

# generate new html base scene component in a module (ie. "button" html-component in @things-factory/scene-switch module)
$ yarn generate html-component
  ? What is target package's name? Ex. switch > # type "switch"
  ? What should this component's name be? Ex. button > # type "button"

# generate new data source scene component in a module (ie. "button" component in @things-factory/scene-switch module)
$ yarn generate data-source
  ? What is target package's name? Ex. switch > # type "switch"
  ? What should this component's name be? Ex. button > # type "button"

# generate new data transform scene component in a module (ie. "button" component in @things-factory/scene-switch module)
$ yarn generate data-transform
  ? What is target package's name? Ex. switch > # type "switch"
  ? What should this component's name be? Ex. button > # type "button"
```

## References

- https://github.com/material-components/material-components-web-components
