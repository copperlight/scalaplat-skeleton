# Scalaplat Skeleton

This is a skeleton project for a single service leveraging scalaplat libraries and conventions.

It is a simple Akka Http Server with two routes defined for the API. There are a few tests which
exercise the API routes.

A Jackson JSON parser is included, to make it easy to start handling machine data. There are a few
tests which exercise the parser.

Build the project, run tests and validation with `make`, which runs the `build` target.

Add license headers and fix `scalafmt` warnings with `make format`.

Produce a fat jar with `make fatjar`; the resulting binary will be about 30MB in size.

Run the fat jar with `make run`.
