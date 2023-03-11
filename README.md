# HiveMQ MQTT Client Documentation

The documentation uses [Jekyll](https://jekyllrb.com/) as a static site generator.

## Setup

To build this documentation, you need [rbenv](https://github.com/rbenv/rbenv), a version switcher for the ruby
programming language, and [bundler](https://bundler.io/), a dependency management tool for ruby gems.

### rbenv

1. Install `rbenv`
    - MacOS:
        1. Install Homebrew
        2. `brew install rbenv ruby-build`
        3. `rbenv init`
    - Linux:
        1. `sudo apt install rbenv`
        2. `rbenv init`
    - Windows: Please follow [rbenv-win](https://github.com/nak1114/rbenv-win)
2. Install the required ruby version in the project directory: `rbenv install`
   
   This uses the version in this directory's `.ruby-version` file.
   The ruby version is also bumped via the `.ruby-version` file.
3. Follow the printed out instructions

### bundler

In the project directory execute:

1. `gem install --user-install bundler`
2. `bundle install`

## Build

1. `bundle exec jekyll serve --livereload --drafts` (add `--incremental` for incremental and shorter builds)
2. Open your browser at http://localhost:4000/
