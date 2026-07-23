import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 2) flatMap no caso mais comum do dia a dia: objetos que têm listas dentro.
 *
 * Cenário: um Pedido tem vários Itens. Quero trabalhar com TODOS os itens
 * de TODOS os pedidos como se fossem uma lista só.
 */
public class FlatMapColecoes {

    record Item(String produto, int quantidade, double precoUnitario) {
        double total() {
            return quantidade * precoUnitario;
        }
    }

    record Pedido(String cliente, List<Item> itens) {}

    public static void main(String[] args) {
        List<Pedido> pedidos = List.of(
                new Pedido("Ana", List.of(
                        new Item("teclado", 1, 250.0),
                        new Item("mouse", 2, 80.0))),
                new Pedido("Bruno", List.of(
                        new Item("monitor", 1, 1200.0))),
                new Pedido("Carla", List.of(
                        new Item("mouse", 1, 80.0),
                        new Item("cadeira", 1, 900.0),
                        new Item("teclado", 3, 250.0)))
        );

        // --- Sem flatMap: laço aninhado ---
        List<Item> todosItensNaMao = new ArrayList<>();
        for (Pedido pedido : pedidos) {
            for (Item item : pedido.itens()) {
                todosItensNaMao.add(item);
            }
        }
        System.out.println("laço aninhado: " + todosItensNaMao.size() + " itens");

        // --- Com flatMap: uma linha ---
        List<Item> todosItens = pedidos.stream()
                .flatMap(pedido -> pedido.itens().stream())
                .toList();
        System.out.println("flatMap:       " + todosItens.size() + " itens");

        // Faturamento total (soma de todos os itens de todos os pedidos)
        double faturamento = pedidos.stream()
                .flatMap(p -> p.itens().stream())
                .mapToDouble(Item::total)
                .sum();
        System.out.printf("faturamento: R$ %.2f%n", faturamento);

        // Produtos distintos vendidos
        List<String> produtos = pedidos.stream()
                .flatMap(p -> p.itens().stream())
                .map(Item::produto)
                .distinct()
                .sorted()
                .toList();
        System.out.println("produtos distintos: " + produtos);

        // Quantidade vendida por produto
        Map<String, Integer> qtdPorProduto = pedidos.stream()
                .flatMap(p -> p.itens().stream())
                .collect(Collectors.groupingBy(
                        Item::produto,
                        Collectors.summingInt(Item::quantidade)));
        System.out.println("qtd por produto: " + qtdPorProduto);

        // Item mais caro entre todos os pedidos
        pedidos.stream()
                .flatMap(p -> p.itens().stream())
                .max(Comparator.comparingDouble(Item::total))
                .ifPresent(item -> System.out.println("item de maior valor: " + item));

        // --- flatMap preservando de qual pedido veio o item ---
        // Perceba: dentro da lambda dá pra usar as duas variáveis (pedido e item),
        // então nada impede de criar um novo objeto combinando os dois.
        record LinhaRelatorio(String cliente, String produto, double total) {}

        List<LinhaRelatorio> relatorio = pedidos.stream()
                .flatMap(pedido -> pedido.itens().stream()
                        .map(item -> new LinhaRelatorio(
                                pedido.cliente(), item.produto(), item.total())))
                .toList();

        System.out.println("\nrelatório:");
        relatorio.forEach(linha -> System.out.println("  " + linha));

        // --- flatMap sobre um Map<K, List<V>> ---
        Map<String, List<String>> habilidades = new LinkedHashMap<>();
        habilidades.put("Ana", List.of("Java", "SQL"));
        habilidades.put("Bruno", List.of("Java", "Kotlin", "Docker"));

        List<String> todasHabilidades = habilidades.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();
        System.out.println("\nhabilidades do time: " + todasHabilidades);
    }
}
