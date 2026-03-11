const debug = require('debug')('things-factory:operato-wcs-system-ui:routes');
process.on('bootstrap-module-global-public-route', (app, globalPublicRouter) => {
    /*
     * can add global public routes to application (auth not required, tenancy not required)
     *
     * ex) routes.get('/path', async(context, next) => {})
     * ex) routes.post('/path', async(context, next) => {})
     */
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