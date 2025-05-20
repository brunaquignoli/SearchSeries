package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    Scanner entrada = new Scanner(System.in);

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=68cc5b8a";

    private ConsumoApi  consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    public void exibeMenu(){
        System.out.println("qual série/filme você deseja acessar?");
        var pesquisa = entrada.nextLine();

        var json = consumo.obterDados(ENDERECO + pesquisa.replace(" ", "+") + API_KEY);

        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        // printa os dados gerais da série
//        System.out.println(dados);

        System.out.println("Título da série: " + dados.titulo() +
                "\nQuantidade total de temporadas: " + dados.totalTemporadas() +
                "\nAvaliação geral da série: " + dados.avaliacao() +
                "\n");


        List<DadosTemporada> temporadas = new ArrayList<>();

		for (int i = 1; i<= dados.totalTemporadas(); i++) {
			json = consumo.obterDados(ENDERECO + pesquisa.replace(" ", "+") + "&season=" + i + API_KEY);
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);

		}

        // printa sobre cada episódio de cada temporada, aquela bagunça
//		temporadas.forEach(t ->{
//            System.out.println(t);
//            System.out.println("\n");
//        });

//        for(int i = 0; i < dados.totalTemporadas(); i++){
//            List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
//            for(int j = 0; j < episodiosTemporada.size(); j++){
//                System.out.println(episodiosTemporada.get(j).titulo());
//            }
//        }

        // printa o nome de cada episódio
//        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        // treino de peek + top 10 episódios
//        List<DadosEpisodio> dadosEpisodios = temporadas.stream().flatMap(t -> t.episodios().stream()).collect(Collectors.toList());
//        System.out.println("\n TOP 10 EPISÓDIOS!!");
//        dadosEpisodios.stream()
//                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
//                .peek(e -> System.out.println( "Primeiro filtro (N/A) " + e))
//                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
//                .peek(e -> System.out.println( "Ordenação " + e))
//                .limit(10)
//                .peek(e -> System.out.println( "Limite " + e))
//                .map(e -> e.titulo().toUpperCase())
//                .peek(e -> System.out.println( "Mapeamento " + e))
//                .forEach(System.out::println);

        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d)))
                .collect(Collectors.toList());
        //isso que printa o bagulho feio
        episodios.forEach(System.out::println);

        System.out.println("Que episódio voê está procurando? (vale trecho!)");
        var trechoTitulo = entrada.nextLine();

        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
                .findFirst();
        if(episodioBuscado.isPresent()){
            System.out.println("Episódio encontrado!");
            System.out.println("Temporada: " + episodioBuscado.get().getTemporada() +
                    ", Nome completo: " + episodioBuscado.get().getTitulo() +
                    ", Episódio de número: " + episodioBuscado.get().getNumeroEpisodio());
        } else {
            System.out.println("Episódio não encontrado!");
        }

        System.out.println("A partir de que ano você deseja ver os episódios? ");
        var ano = entrada.nextInt();
        entrada.nextLine();

        LocalDate dataBusca = LocalDate.of(ano, 1, 1);

        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        episodios.stream().filter(e -> e.getDataDeLancamento() != null && e.getDataDeLancamento().isAfter(dataBusca))
                .forEach(e -> System.out.println("Temporada: " + e.getTemporada()
                        + ", episódio: " + e.getTitulo()
                        + ", Data Lançamento: " + e.getDataDeLancamento().format(formatador)
                + "\n"));

        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao()> 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                Collectors.averagingDouble(Episodio::getAvaliacao)));

//        System.out.println(avaliacoesPorTemporada);

        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao()> 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));




        String media = String.format("%.2f", est.getAverage());
        String maior = String.format("%.2f", est.getMax());
        String menor = String.format("%.2f", est.getMin());

        System.out.println("Avaliações gerais da série!!!");
        System.out.println("Média: " + media);
        System.out.println("Melhor episódio: " + maior);
        System.out.println("Pior episódio: " + menor);
        System.out.println("Total episódios contados: " + est.getCount());
        System.out.println("    *Só foram contabilizados episódios com avaliação diferente de N/A!");
    }


}
