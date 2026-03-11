"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const debug = require('debug')('things-factory:operatofill:routes');
const env_1 = require("@things-factory/env");
process.on('bootstrap-module-global-public-route', (app, globalPublicRouter) => {
    /*
     * can add global public routes to application (auth not required, tenancy not required)
     *
     * ex) routes.get('/path', async(context, next) => {})
     * ex) routes.post('/path', async(context, next) => {})
     */
    globalPublicRouter.get('/env/operato/base_url', async (context, next) => {
        const { baseUrl } = env_1.config.get('operato');
        context.body = {
            success: true,
            value: baseUrl
        };
    });
});
process.on('bootstrap-module-global-private-route', (app, globalPrivateRouter) => {
    /*
     * can add global private routes to application (auth required, tenancy not required)
     */
});
process.on('bootstrap-module-domain-public-route', (app, domainPublicRouter) => {
    /*
     * can add domain public routes to application (auth not required, tenancy required)
     */
});
process.on('bootstrap-module-domain-private-route', (app, domainPrivateRouter) => {
    /*
     * can add domain private routes to application (auth required, tenancy required)
     */
});
//# sourceMappingURL=routes.js.map