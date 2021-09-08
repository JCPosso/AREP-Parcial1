package edu.eci.arep;

import java.net.*;
import java.io.*;
import java.util.HashMap;

public class HttpServer {
    public HttpServer(){}

    /*
     *   Método que inicializa el servidor
     * */
    public void start() throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        boolean running = true;
        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println( "Listo para recibir ..." );
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println( "Accept failed." );
                System.exit( 1 );
            }
            serveConnection(clientSocket);
            clientSocket.close();

        }
        serverSocket.close();
    }

    /**
     * Metodo  procesar las peticiones.
     * @param clientSocket conexion Socket con cliente
     * @throws IOException Error de lectura del cliente socket
     */
    public void serveConnection (Socket clientSocket) throws IOException {
        OutputStream out= clientSocket.getOutputStream();
        BufferedReader in = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
        String inputLine, outputLine;
        HashMap<String,String> request  = new HashMap<String,String>();
        boolean ready = false;
        while ((inputLine = in.readLine()) != null) {
            System.out.println("Received: " + inputLine);
            if(!ready){
                request.put("rq", inputLine);
                ready =true;
            } else {
                String[] line = inputLine.split( ":" );
                if (line.length>1 )
                    request.put( line[0], line[1] );
            }
            if (!in.ready()) {
                break;
            }
        }
        getResource(request,out);
        in.close();
    }

    /**
     * Genera la respuesta de la peticion del cliente
     * @param ClientSocket OutputStream del socket del cliente
     * @param response información procesada de la petición del cliente
     * @throws IOException Error de escritura del recurso
     */
    public  void getResource(HashMap<String,String> response, OutputStream ClientSocket) throws IOException {
        String[] requestLine = response.get( "rq" ).split( " " );
        PrintWriter printWriter = new PrintWriter( ClientSocket, true );
        if(requestLine[1].contains("/clima")) {
            String city = requestLine[1];
            String path = city;
            String[] route = path.split( "\\/" );
            String city2 = route[route.length - 1];
            System.out.println( city2 );
            String answer = "";
            URL url = new URL( "https://api.openweathermap.org/data/2.5/weather?q=" + city2 + "&appid=602d8e6663a4731d5a708462db8af16b" );
            getResponse(printWriter);
            ClientSocket.close();
        }else if (requestLine[1].contains("/consulta?lugar=")){
            String urlpath = requestLine[1].replace("/consulta?lugar=","");
            String newUrl = "https://api.openweathermap.org/data/2.5/weather?q="+urlpath+"602d8e6663a4731d5a708462db8af16b";
            String newJSON= BufferJson(newUrl);
            System.out.println(newJSON);
            printWriter.println(newJSON);
        }
        printWriter.close();

    }
    private String BufferJson(String newUrl) throws IOException {
        String inputLine = null;
        StringBuffer JSON = new StringBuffer();
        URL siteURL = new URL(newUrl);
        URLConnection urlConnection = siteURL.openConnection();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
            while ((inputLine = reader.readLine()) != null) {
                JSON.append(inputLine);
            }
        } catch (IOException x) {
            System.err.println(x);
        }
        return JSON.toString();
    }
    private void getResponse(PrintWriter clientsocket) {
        String content =
                    "HTTP/1.1 200 OK\r\n"+
                    "<head>\n" +
                    "<meta charset=\"utf-8\">\n" +
                    "<link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css\" integrity=\"sha384-MCw98/SFnGE8fJT3GXwEOngsV7Zt27NXFoaoApmYm81iuXoPkFOJwJ8ERdknLPMO\" crossorigin=\"anonymous\">\n" +
                    "<script src=\"https://code.jquery.com/jquery-3.3.1.slim.min.js\" integrity=\"sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo\" crossorigin=\"anonymous\"></script>\n" +
                    "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js\" integrity=\"sha384-ZMP7rVo3mIykV+2+9J3UJ46jBk0WLaUAdn689aCwoqbBJiSnjAK/l8WvCWPIPm49\" crossorigin=\"anonymous\"></script>\n" +
                    "<script src=\"https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/js/bootstrap.min.js\" integrity=\"sha384-ChfqqxuZUCnJSK3+MXmPNIyE6ZbWh2IMqE241rYiqJxyMiZ6OW/JmZQ5stwEULTy\" crossorigin=\"anonymous\"></script>\n" +
                    "<title>Clima</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<script>\n" +
                    "function generar(){\n" +
                    "json = window.open(\"consulta\"+\"?lugar=\"+document.getElementById(\"city\").value);}" +
                    "</script>\n" +
                    " <br><br><br>\n" +
                    "Ciudad: <input type=\"city\"id=\"city\"/><br><br>\n" +
                    "<button onclick=\"generar()\" >generar</button>\n" +
                    "</body>\n" +
                    "</html>\n";
            clientsocket.println( content );
        }
    }
}