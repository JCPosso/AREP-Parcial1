window.onload=function(){
    var mb = document.getElementById("myform");
    mb.addEventListener("submit", (e)=>{
        e.preventDefault();
        const formData= new FormData(e.currentTarget)
        traerClima(formData).then (data=>{
            pegarInfo()
        })
    });
};
function pegarInfo(obj){
    let text = ''
    document.getElementById("demo").innerHTML = text;
}
async function traerAlpha(formData){
    const clima = formData.get("data-exchange");
    console.log(clima+"  ");
    const response = await fetch("https://heroku-app-arep.herokuapp.com/clonsulta?lugar="+clima)
    const data = await response.json()
    return data
};