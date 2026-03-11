module.exports = {
  subdomain: "logisid",
  port: 5701,
  accessTokenCookieKey: 'access_token',
  operato: {
    baseUrl: 'http://localhost:9190/rest',
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
  email: {
    host: 'smtp.office365.com', // your sender-email smtp host
    port: 587, // smtp server port
    secure: false, // true for 465, false for other ports
    auth: {
      user: 'your sender-email',
      pass: 'your sender-email password' // generated ethereal password
    },
    secureConnection: false,
    tls: {
      ciphers: 'SSLv3'
    }
  },
  logger: {
    file: {
      filename: 'logs/application-%DATE%.log',
      datePattern: 'YYYY-MM-DD-HH',
      zippedArchive: false,
      maxSize: '20m',
      maxFiles: '2d',
      level: 'debug'
    },
    console: {
      level: 'silly'
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
    logging: ['error', 'query']
  }
}