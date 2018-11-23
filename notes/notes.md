# Notes

## [Customizing overlay](http://hapifhir.io/doc_server_tester.html#Adding_the_Overlay)

## Remove all images starting with a name

```
docker rmi $(docker images |grep 'imagename')

```