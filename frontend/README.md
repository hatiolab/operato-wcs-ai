
# very first time

```
$ yarn install
$ yarn build # build all packages
$ DEBUG=things-factory:* yarn workspace @things-factory/reference-app run serve:dev
$ DEBUG=things-factory:* yarn workspace @things-factory/reference-app run serve:client
```


# after a dependent package(ie. @things-factory/dependa) modified

```
$ yarn workspace @things-factory/dependa build
$ DEBUG=things-factory:* yarn workspace @things-factory/reference-app run serve:dev
```

# run application (ie. @things-factory/reference-app) in production mode
```
$ yarn workspace @things-factory/reference-app build
$ yarn workspace @things-factory/reference-app build:client
$ yarn workspace @things-factory/reference-app run serve
```

## License

MIT &copy; [Hatiolab](https://www.hatiolab.com/), see [LICENSE](LICENSE.md) for details.

<a href="http://www.hatiolab.com/"><img src="https://www.hatiolab.com/assets/img/logo.png" alt="Hatiolab" width="200" /></a>
