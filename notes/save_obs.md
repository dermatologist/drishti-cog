## Save observation

```
handleUpdate(e) {
        e.preventDefault();
        const resid = $("#resource_id").val();
        const posturl = `/fhir/fhir/${$("#resource_type").val()}/${resid}`;
        //postdata = JSON.stringify( { "resource-create-body": jsonString, "resource-create-id": resid } )
        const {jsonString, resourceType} = this.buildJson();
        return $.ajax(posturl, {
            type: "PUT",
            //dataType: 'json'
            contentType: "application/json+fhir",
            data: jsonString,
            error(jqXHR, textStatus, errorThrown) {
                return $("body").append(`AJAX Error: ${textStatus}`);
            },
            success(data, textStatus, jqXHR) {
                return $("body").append(`Successful AJAX call: ${data}`);
            }
        });
    }

``` 