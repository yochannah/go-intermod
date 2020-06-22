# gointermod

**Demo:** http://gointermod.apps.intermine.org/#/

A [re-frame](https://github.com/Day8/re-frame) application designed to search GO terms across multile InterMine Instances.

## Licence
This tool is open source, licenced under LGPL 2.1.

## Development Mode

### Compile css:

Compile css file once.

```
lein less once
```

Automatically recompile css file on change.

```
lein less auto
```

### Run application:

```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

## Production Build

```
lein clean
lein cljsbuild once min
```

To deploy to dokku visit [https://github.com/intermine/go-intermod-webapp](https://github.com/intermine/go-intermod-webapp) for more info
