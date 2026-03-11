[comment]: # 'NOTE: This file is generated and should not be modify directly. Update `templates/ROOT_README.hbs.md` instead'

# Things Factory&trade; Micro-Module Edition

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE.md)
[![Build Status](https://travis-ci.org/hatiolab/things-factory.svg?branch=master)](https://travis-ci.org/hatiolab/things-factory)
[![codecov](https://codecov.io/gh/hatiolab/things-factory/branch/master/graph/badge.svg)](https://codecov.io/gh/hatiolab/things-factory)
[![lerna](https://img.shields.io/badge/maintained%20with-lerna-cc00ff.svg)](https://lernajs.io/)

ThingsFactory&trade; is a software framework brand for the production of commercial-level web applications licensed by [Hatiolab](https://www.hatiolab.com).

ThingsFactory&trade; Micro-Module Edition implements an architecture that produces web applications by composing fine-grained micro modules.

These modules compose together to help you create performant modern JS apps that you love to develop and test. These packages are developed primarily to be used on top of the stack we like best for our JS apps; Typescript for the flavor, Koa for the server, LitElement for UI, Apollo for data fetching, and Jest for tests. That said, you can mix and match as you like.

## Usage

The things-factory repo is managed as a monorepo that is composed of {{jsPackages.length}} npm packages.
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
$ yarn workspace @things-factory/operato-mms build
$ yarn workspace @things-factory/operato-mms build:client
$ yarn workspace @things-factory/operato-mms run serve

# The way to use the config file is the same as before.
# Don't forget to give a config file to make the app run.
```

```
# publish packages that have changed since the last release
$ yarn release

# publish all packages
$ yarn release:force

# Don't forget to commit all changes before release
```

```
# dockerize application (ie. @things-factory/operato-mms)
$ yarn workspace @things-factory/operato-mms build
$ yarn workspace @things-factory/operato-mms build:client
$ yarn workspace @things-factory/operato-mms docker # build docker image
$ yarn workspace @things-factory/operato-mms docker:push # push docker image to docker repository
```

```
# generate new application (ie. @things-factory/operato-xyz)
$ yarn generate app
  ? What should this application's name be? Ex. operato-abc > # type "operato-xyz"

# generate new module (ie. @things-factory/notification)
$ yarn generate module
  ? What should this module's name be? Ex. menu > # type "notification"

# generate new entity in a module (ie. "sms" entity in @things-factory/notification module)
# please use 'service' instead if you are using typegraphql way for new entity.
$ yarn generate entity
  ? What is target package's name? Ex. biz-base, operato-mms > # type "notification"
  ? What should this entitie's name be? Ex. company, company-ext > # type "sms"

# generate new service in a module (ie. "sms" service in @things-factory/notification module)
$ yarn generate service
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

### Package Index

| Package | Version | Description |
| ------- | ------- | ----------- |
{{#each jsPackages}}
| [{{name}}](packages/{{name}}) | <a href="https://badge.fury.io/js/%40things-factory%2F{{name}}"><img src="https://badge.fury.io/js/%40things-factory%2F{{name}}.svg" width="200px" /></a> | {{{description}}} |
{{/each}}

## Want to contribute?

Check out our [Contributing Guide](./.github/CONTRIBUTING.md)

## License

MIT &copy; [Hatiolab](https://www.hatiolab.com/), see [LICENSE](LICENSE.md) for details.

<a href="http://www.hatiolab.com/"><img src="https://www.hatiolab.com/assets/img/logo.png" alt="Hatiolab" width="200" /></a>
