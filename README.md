# HiveMQ MQTT Client Documentation

The documentation uses [Jekyll](https://jekyllrb.com/) as a static site generator.

It requires a modern and updated install of Ruby.

## Setup

1. (Skip on MacOS as already installed ) Install Ruby development environment: https://jekyllrb.com/docs/installation/
2. `gem install --user-install bundler`
3. `echo 'export PATH="$HOME/.gem/ruby/2.6.0/bin:$PATH"' >> ~/.zshrc` (check if you need to replace `2.6.0` with a newer version)
4. In the project directory execute
    1. `bundle config set --local path 'vendor/bundle'`
    2. `bundle install`

## Build

1. `bundle exec jekyll serve --livereload --drafts` (add `--incremental` for incremental and shorter builds)
2. Open your browser at http://localhost:4000/
