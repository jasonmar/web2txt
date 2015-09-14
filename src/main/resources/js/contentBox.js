function setContent(x) {
    x = x.replace(/(?:\r\n|\r|\n)/g, '<br />');
    document.getElementById("contentBox").innerHTML = x;
}
function getContent() {
    var xmlHttp = new XMLHttpRequest();
    var url = "/txt/" + document.getElementById("uri").value;
    xmlHttp.open("GET", url, true);
    xmlHttp.onreadystatechange = function() {
      if(xmlHttp.readyState == 4) {
        if (xmlHttp.status == 200) {
          setContent(xmlHttp.responseText);
        } else {
          setContent("There was an error.");
        }
      }
    }
    xmlHttp.send();
}
