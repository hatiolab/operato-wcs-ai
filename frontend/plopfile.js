const { readdirSync, existsSync } = require('fs')
const path = require('path')
const { camelCase, startCase } = require('lodash')
const { plural } = require('pluralize')
const { version } = require('./lerna.json')

const jsPackages = getPackages('js')
const timestamp = Date.now()

module.exports = function (plop) {
  plop.setGenerator('app', {
    description: 'Create a new application package from the scratch',
    prompts: [
      {
        type: 'input',
        name: 'name',
        message: "What should this application's name be? Ex. operato-abc",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      },
      {
        type: 'input',
        name: 'description',
        message: "What should this application's description be?",
        filter: stripDescription
      }
    ],
    actions: [
      {
        type: 'addMany',
        destination: 'packages/{{name}}',
        base: 'templates/app',
        templateFiles: 'templates/app/**/*',
        force: false,
        data: {
          version
        }
      }
    ]
  })

  plop.setGenerator('brand', {
    description: 'Create a new application brand package from the scratch',
    prompts: [
      {
        type: 'input',
        name: 'name',
        message: "What should this application's name be? Ex. operato-abc",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      },
      {
        type: 'input',
        name: 'description',
        message: "What should this application's description be?",
        filter: stripDescription
      }
    ],
    actions: [
      {
        type: 'addMany',
        destination: '../{{name}}',
        base: 'templates/brand',
        templateFiles: 'templates/brand/**/*',
        force: false,
        data: {
          version
        }
      }
    ]
  })

  plop.setGenerator('module', {
    description: 'Create a new module from the scratch',
    prompts: [
      {
        type: 'input',
        name: 'name',
        message: "What should this module's name be? Ex. menu",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      },
      {
        type: 'input',
        name: 'description',
        message: "What should this module's description be?",
        filter: stripDescription
      }
    ],
    actions: [
      {
        type: 'addMany',
        destination: 'packages/{{name}}',
        base: 'templates/module',
        templateFiles: 'templates/module/**/*',
        force: false,
        data: {
          version
        }
      }
    ]
  })

  plop.setGenerator('migration', {
    description: 'Generate migration from the scratch',
    prompts: [
      {
        type: 'input',
        name: 'packageName',
        message: "What is target package's name? Ex. biz-base, operato-mms",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      },
      {
        type: 'input',
        name: 'name',
        message: "What should this entitie's name be? Ex. company, company-ext",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      }
    ],
    actions: [
      {
        type: 'add',
        path: `packages/{{packageName}}/server/migrations/${timestamp}-Seed{{pascalCase name}}.ts`,
        templateFile: 'templates/entity/migrations/timestamped-SeedEntity.ts.hbs',
        data: {
          timestamp
        }
      }
    ]
  })

  plop.setGenerator('entity', {
    description: 'Generate entity from scratch',
    prompts: [
      {
        type: 'input',
        name: 'packageName',
        message: "What is target package's name? Ex. biz-base, operato-mms",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      },
      {
        type: 'input',
        name: 'name',
        message: "What should this entitie's name be? Ex. company, company-ext",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      }
    ],
    actions: [
      {
        type: 'add',
        path: 'packages/{{packageName}}/server/entities/{{name}}.ts',
        templateFile: 'templates/entity/entities/entity.ts.hbs'
      },
      {
        type: 'modify',
        path: 'packages/{{packageName}}/server/entities/index.ts',
        transform: (file, { name }) => {
          const pascalCaseName = startCase(camelCase(name)).replace(/ /g, '')
          file = file.replace(/^/, `import { ${pascalCaseName} } from './${name}'\n`)
          file = file.replace(/export const entities = \[/, `export const entities = [\n\t${pascalCaseName},`)
          file = file.replace(/export {/, `export {\n\t${pascalCaseName},`)
          return file
        }
      },
      {
        type: 'addMany',
        destination: 'packages/{{packageName}}/server/graphql/resolvers/{{name}}',
        base: 'templates/entity/graphql/resolvers/entity',
        templateFiles: 'templates/entity/graphql/resolvers/entity/*.ts.hbs',
        force: false,
        data: {
          pluralPascalCase: function (name) {
            return startCase(camelCase(plural(name))).replace(/ /g, '')
          },
          pluralCamelCase: function (name) {
            return camelCase(plural(name))
          }
        }
      },
      {
        type: 'modify',
        path: 'packages/{{packageName}}/server/graphql/resolvers/index.ts',
        transform: (file, { name }) => {
          file = file.replace(/$/, `\nexport * from './${name}'`)
          return file
        }
      },
      {
        type: 'addMany',
        destination: 'packages/{{packageName}}/server/graphql/types/{{name}}',
        base: 'templates/entity/graphql/types/entity',
        templateFiles: 'templates/entity/graphql/types/entity/*.ts.hbs',
        force: false,
        data: {
          pluralPascalCase: function (name) {
            return startCase(camelCase(plural(name))).replace(/ /g, '')
          },
          pluralCamelCase: function (name) {
            return camelCase(plural(name))
          }
        }
      },
      {
        type: 'modify',
        path: 'packages/{{packageName}}/server/graphql/types/index.ts',
        transform: (file, { name }) => {
          file = file.replace(/$/, `\nexport * from './${name}'`)

          return file
        }
      },
      {
        type: 'add',
        path: `packages/{{packageName}}/server/migrations/${timestamp}-Seed{{pascalCase name}}.ts`,
        templateFile: 'templates/entity/migrations/timestamped-SeedEntity.ts.hbs',
        data: {
          timestamp
        }
      }
    ]
  })

  plop.setGenerator('service', {
    description: 'Generate service from the scratch',
    prompts: [
      {
        type: 'input',
        name: 'packageName',
        message: "What is target package's name? (Ex. biz-base, operato-mms)",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      },
      {
        type: 'input',
        name: 'name',
        message: "What should this entitie's name be? (Ex. company, company-ext)",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      },
      {
        type: 'confirm',
        name: 'withMigration',
        message: 'Do you want generate seed migration sample for this entity?'
      }
    ],
    actions: function (data) {
      const actions = [
        {
          type: 'add',
          path: `packages/{{packageName}}/server/service/index.ts`,
          templateFile: 'templates/service/service/index.ts.hbs',
          skipIfExists: true,
          force: false
        },
        {
          type: 'addMany',
          destination: 'packages/{{packageName}}/server/service/{{name}}',
          base: 'templates/service/service/entity',
          templateFiles: 'templates/service/service/entity/*.ts.hbs',
          skipIfExists: true,
          force: false,
          data: {
            pluralPascalCase: function (name) {
              return startCase(camelCase(plural(name))).replace(/ /g, '')
            },
            pluralCamelCase: function (name) {
              return camelCase(plural(name))
            }
          }
        },
        {
          type: 'modify',
          path: 'packages/{{packageName}}/server/service/index.ts',
          transform: (file, { name }) => {
            const pascalCaseName = startCase(camelCase(name)).replace(/ /g, '')
            file = file.replace(
              /\/\* EXPORT ENTITY TYPES \*\//,
              `/* EXPORT ENTITY TYPES */\nexport * from './${name}/${name}'`
            )
            file = file.replace(
              /\/\* IMPORT ENTITIES AND RESOLVERS \*\//,
              `/* IMPORT ENTITIES AND RESOLVERS */\nimport { entities as ${pascalCaseName}Entities, resolvers as ${pascalCaseName}Resolvers } from './${name}'`
            )
            file = file.replace(/\/\* ENTITIES \*\//, `/* ENTITIES */\n\t...${pascalCaseName}Entities,`)
            file = file.replace(
              /\/\* RESOLVER CLASSES \*\//,
              `/* RESOLVER CLASSES */\n\t\t...${pascalCaseName}Resolvers,`
            )
            return file
          }
        }
      ]

      if (data.withMigration) {
        actions.push({
          type: 'add',
          path: `packages/{{packageName}}/server/migrations/${timestamp}-Seed{{pascalCase name}}.ts`,
          templateFile: 'templates/service/migrations/timestamped-SeedEntity.ts.hbs',
          force: false,
          data: {
            timestamp
          }
        })
      }

      return actions
    }
  })

  plop.setGenerator('page', {
    description: 'Generate client page from the scratch',
    prompts: [
      {
        type: 'input',
        name: 'packageName',
        message: "What is target package's name? Ex. biz-base, operato-mms",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      },
      {
        type: 'input',
        name: 'name',
        message: "What should this pages's name be? Ex. abc-viewer",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      }
    ],
    actions: [
      {
        type: 'add',
        path: 'packages/{{packageName}}/client/pages/{{name}}.js',
        templateFile: 'templates/page/page.js.hbs',
        force: false
      }
    ]
  })

  plop.setGenerator('scene-module', {
    description: 'Create a new scene-module from the scratch',
    prompts: [
      {
        type: 'input',
        name: 'name',
        message: "What should this module's name be? Ex. scene-random",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      },
      {
        type: 'input',
        name: 'description',
        message: "What should this module's description be?",
        filter: stripDescription
      }
    ],
    actions: [
      {
        type: 'addMany',
        destination: 'packages/{{name}}',
        base: 'templates/scene-module',
        templateFiles: 'templates/scene-module/**/*',
        force: false,
        data: {
          version
        }
      }
    ]
  })

  plop.setGenerator('scene-component', {
    description: 'Generate scene-component from the scratch',
    prompts: [
      {
        type: 'input',
        name: 'packageName',
        message: "What is target package's name? Ex. switch",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      },
      {
        type: 'input',
        name: 'name',
        message: "What should this component's name be? Ex. button",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      }
    ],
    actions: [
      {
        type: 'add',
        path: 'packages/{{packageName}}/src/{{name}}.js',
        templateFile: 'templates/scene-component/component.js.hbs',
        force: false
      },
      {
        type: 'modify',
        path: 'packages/{{packageName}}/src/index.js',
        transform: (file, { name }) => {
          const pascalCaseName = startCase(camelCase(name)).replace(/ /g, '')
          return file.replace(/$/, `\nexport { default as ${pascalCaseName} } from './${name}'`)
        }
      }
    ]
  })

  plop.setGenerator('scene-html-component', {
    description: 'Generate html base scene component from the scratch',
    prompts: [
      {
        type: 'input',
        name: 'packageName',
        message: "What is target package's name? Ex. switch",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      },
      {
        type: 'input',
        name: 'name',
        message: "What should this component's name be? Ex. button",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      }
    ],
    actions: [
      {
        type: 'add',
        path: 'packages/{{packageName}}/src/{{name}}.js',
        templateFile: 'templates/scene-html-component/component.js.hbs',
        force: false
      },
      {
        type: 'modify',
        path: 'packages/{{packageName}}/src/index.js',
        transform: (file, { name }) => {
          const pascalCaseName = startCase(camelCase(name)).replace(/ /g, '')
          return file.replace(/$/, `\nexport { default as ${pascalCaseName} } from './${name}'`)
        }
      }
    ]
  })

  plop.setGenerator('scene-data-source', {
    description: 'Generate data source scene component from the scratch',
    prompts: [
      {
        type: 'input',
        name: 'packageName',
        message: "What is target package's name? Ex. switch",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      },
      {
        type: 'input',
        name: 'name',
        message: "What should this component's name be? Ex. button",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      }
    ],
    actions: [
      {
        type: 'add',
        path: 'packages/{{packageName}}/src/{{name}}.js',
        templateFile: 'templates/scene-data-source/component.js.hbs',
        force: false
      },
      {
        type: 'add',
        path: 'packages/{{packageName}}/src/{{name}}.png',
        templateFile: 'templates/scene-data-source/component.png',
        force: false
      },
      {
        type: 'modify',
        path: 'packages/{{packageName}}/src/index.js',
        transform: (file, { name }) => {
          const pascalCaseName = startCase(camelCase(name)).replace(/ /g, '')
          return file.replace(/$/, `\nexport { default as ${pascalCaseName} } from './${name}'`)
        }
      }
    ]
  })

  plop.setGenerator('scene-data-transform', {
    description: 'Generate data transform scene component from the scratch',
    prompts: [
      {
        type: 'input',
        name: 'packageName',
        message: "What is target package's name? Ex. switch",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      },
      {
        type: 'input',
        name: 'name',
        message: "What should this component's name be? Ex. button",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      }
    ],
    actions: [
      {
        type: 'add',
        path: 'packages/{{packageName}}/src/{{name}}.js',
        templateFile: 'templates/scene-data-transform/component.js.hbs',
        force: false
      },
      {
        type: 'add',
        path: 'packages/{{packageName}}/src/{{name}}.png',
        templateFile: 'templates/scene-data-transform/component.png',
        force: false
      },
      {
        type: 'modify',
        path: 'packages/{{packageName}}/src/index.js',
        transform: (file, { name }) => {
          const pascalCaseName = startCase(camelCase(name)).replace(/ /g, '')
          return file.replace(/$/, `\nexport { default as ${pascalCaseName} } from './${name}'`)
        }
      }
    ]
  })

  plop.setGenerator('scene-container', {
    description: 'Generate container scene component from the scratch',
    prompts: [
      {
        type: 'input',
        name: 'packageName',
        message: "What is target package's name? Ex. switch",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      },
      {
        type: 'input',
        name: 'name',
        message: "What should this container component's name be? Ex. button",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      }
    ],
    actions: [
      {
        type: 'add',
        path: 'packages/{{packageName}}/src/{{name}}.js',
        templateFile: 'templates/scene-container/component.js.hbs',
        force: false
      },
      {
        type: 'modify',
        path: 'packages/{{packageName}}/src/index.js',
        transform: (file, { name }) => {
          const pascalCaseName = startCase(camelCase(name)).replace(/ /g, '')
          return file.replace(/$/, `\nexport { default as ${pascalCaseName} } from './${name}'`)
        }
      }
    ]
  })

  plop.setGenerator('help', {
    description: 'Generate inline help page',
    prompts: [
      {
        type: 'input',
        name: 'packageName',
        message: "What is target package's name? Ex. biz-base, operato-mms",
        validate: validatePackageName,
        filter: plop.getHelper('kebabCase')
      },
      {
        type: 'input',
        name: 'topic',
        message: "What should this help page's topic be? Ex. integration/adapter/mqtt"
      }
    ],
    actions: [
      {
        type: 'add',
        path: 'packages/{{packageName}}/helps/{{topic}}.md',
        templateFile: 'templates/help/template.md.hbs',
        force: false
      }
    ]
  })

  plop.setGenerator('docs', {
    description: 'Generate root repo documentation',
    prompts: [],
    actions: [
      {
        type: 'add',
        path: 'README.md',
        templateFile: 'templates/docs/ROOT_README.hbs.md',
        force: true,
        data: { jsPackages }
      }
    ]
  })
}

function getPackages(type = 'js') {
  const packagesPath = path.join(__dirname, type === 'js' ? 'packages' : 'gems')

  return readdirSync(packagesPath).reduce((acc, packageName) => {
    const packageJSONPath = path.join(packagesPath, packageName, 'package.json')

    if (existsSync(packageJSONPath)) {
      const { name, description } = require(packageJSONPath)

      acc.push({ name: name.replace('@things-factory/', ''), description })
    }

    return acc
  }, [])
}

function validatePackageName(name) {
  return (
    /^(?:@[a-z0-9-*~][a-z0-9-*._~]*\/)?[a-z0-9-~][a-z0-9-._~]*$/g.test(`@things-factory/${name}`) ||
    `Your package name (@things-factory/${name}) does not confirm to npm rules!`
  )
}

function stripDescription(desc) {
  return desc.replace(/[.\s]*$/g, '').replace(/^\s*/g, '')
}
