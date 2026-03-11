module.exports = {
  subdomain: 'logisid',
  port: 5908,
  accessTokenCookieKey: 'access_token',
  operato: {
    baseUrl: 'http://localhost:9190/rest',
  },
  requestBody: {
    formLimit: '10mb',
    jsonLimit: '10mb',
    textLimit: '10mb',
  },
  fileUpload: {
    maxFileSize: '10mb',
    maxFiles: 10
  },
  awsAppSync: {
    apiUrl: '',
    apiKey: ''
  },
  /** 
 * Domain Primary Colulumn Configuration
 * support 
 * Types : "int","int2","int4","int8","integer","tinyint","smallint","mediumint","bigint",'uuid'
 * Strategy = 'uuid','rowid',"increment","identity"
 * by defualt use uuid auto generated id
 * ie.
 *  domainPrimaryOption:{
 *   type:'int8',
 *   strategy:null
 * }
*/
  domainPrimaryOption: {
    type: 'int8',
    strategy: null
  },
  logger: {
    file: {
      filename: 'logs/application-%DATE%.log',
      datePattern: 'YYYY-MM-DD-HH',
      zippedArchive: false,
      maxSize: '20m',
      maxFiles: '1d',
      level: 'debug'
    },
    console: {
      level: 'debug'
    }
  },
  ormconfig: {
    name: 'default',
    type: 'postgres',
    database: 'operato2',
    host: '60.196.69.234',
    port: 3298,
    username: 'operato2',
    password: 'dev!tools#',
    synchronize: false,
    logging: ['debug', 'query']
  }
}