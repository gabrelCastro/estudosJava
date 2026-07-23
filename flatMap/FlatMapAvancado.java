import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 4) Casos menos óbvios: arrays, strings, primitivos, produto cartesiano
 *    e as armadilhas mais comuns.
 */
public class FlatMapAvancado {

    public static void main(String[] args) throws Exception {
        arrays();
        strings();
        primitivos();
        produtoCartesiano();
        arquivos();
        armadilhas();
    }

    /** Array de arrays -> stream achatado. */
    static void arrays() {
        int[][] matriz = {{1, 2, 3}, {4, 5}, {6}};

        // ATENÇÃO: para int[] use flatMapToInt + Arrays.stream,
        // não flatMap (int[] não é Stream).
        int soma = Arrays.stream(matriz)
                .flatMapToInt(Arrays::stream)
                .sum();
        System.out.println("soma da matriz: " + soma); // 21

        // Com objetos, flatMap normal resolve:
        String[][] tabuleiro = {{"a1", "a2"}, {"b1", "b2"}};
        List<String> casas = Arrays.stream(tabuleiro)
                .flatMap(Arrays::stream)
                .toList();
        System.out.println("casas: " + casas);
    }

    /** Quebrar textos em palavras / caracteres. */
    static void strings() {
        List<String> frases = List.of(
                "o rato roeu a roupa",
                "do rei de roma"
        );

        // Frases -> palavras
        List<String> palavras = frases.stream()
                .flatMap(frase -> Arrays.stream(frase.split(" ")))
                .toList();
        System.out.println("\npalavras: " + palavras);

        // Contagem de palavras (o clássico word count)
        Map<String, Long> contagem = frases.stream()
                .flatMap(FlatMapAvancado::separarPalavras)
                .collect(Collectors.groupingBy(p -> p, Collectors.counting()));
        System.out.println("contagem: " + contagem);

        // Palavras -> caracteres distintos
        List<String> letras = palavras.stream()
                .flatMap(p -> p.chars().mapToObj(Character::toString))
                .distinct()
                .sorted()
                .toList();
        System.out.println("letras usadas: " + letras);
    }

    private static final Pattern ESPACO = Pattern.compile(" ");

    private static Stream<String> separarPalavras(String frase) {
        // splitAsStream evita criar o array intermediário que o String.split cria
        return ESPACO.splitAsStream(frase);
    }

    /** Variantes para primitivos: flatMapToInt / ToLong / ToDouble. */
    static void primitivos() {
        // Cada número n vira a sequência 1..n
        List<Integer> expandido = IntStream.rangeClosed(1, 4)
                .flatMap(n -> IntStream.rangeClosed(1, n))
                .boxed()
                .toList();
        System.out.println("\nexpandido: " + expandido); // [1, 1,2, 1,2,3, 1,2,3,4]
    }

    /** flatMap aninhado gera combinações (produto cartesiano). */
    static void produtoCartesiano() {
        List<String> naipes = List.of("♠", "♥", "♦", "♣");
        List<String> valores = List.of("A", "K", "Q", "J");

        List<String> baralho = naipes.stream()
                .flatMap(naipe -> valores.stream()
                        .map(valor -> valor + naipe))
                .toList();

        System.out.println("\nbaralho (" + baralho.size() + " cartas): " + baralho);

        // Pares (a, b) com a < b
        List<String> pares = IntStream.rangeClosed(1, 4).boxed()
                .flatMap(a -> IntStream.rangeClosed(a + 1, 4).boxed()
                        .map(b -> "(" + a + "," + b + ")"))
                .toList();
        System.out.println("pares: " + pares);
    }

    /** Ler várias linhas de vários arquivos como um stream só. */
    static void arquivos() throws IOException {
        Path dir = Files.createTempDirectory("flatmap-demo");
        Files.writeString(dir.resolve("a.txt"), "linha 1\nlinha 2");
        Files.writeString(dir.resolve("b.txt"), "linha 3");

        try (Stream<Path> paths = Files.list(dir)) {
            List<String> linhas = paths
                    .sorted()
                    .flatMap(p -> {
                        try {
                            // Files.lines devolve Stream<String> — encaixa direto no flatMap.
                            return Files.lines(p);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .toList();
            System.out.println("\nlinhas dos arquivos: " + linhas);
        }

        // limpeza
        try (Stream<Path> paths = Files.list(dir)) {
            paths.forEach(p -> p.toFile().delete());
        }
        Files.deleteIfExists(dir);
    }

    /** Erros comuns. */
    static void armadilhas() {
        System.out.println("\n--- armadilhas ---");

        // 1) Passar a coleção em vez do stream dela. Não compila:
        //    lista.stream().flatMap(sub -> sub)          // erro: List não é Stream
        //    lista.stream().flatMap(sub -> sub.stream()) // ok

        // 2) Usar Stream.of com um array de objetos: cria UM elemento (o array),
        //    não os elementos dele. Use Arrays.stream.
        String[] arr = {"a", "b"};
        System.out.println("Stream.of(arr).count()     = " + Stream.of(arr).count());      // 2 (varargs, aqui deu certo)
        Integer[] nums = {1, 2, 3};
        System.out.println("Arrays.stream(nums).count()= " + Arrays.stream(nums).count()); // 3
        // O perigo é com int[]: Stream.of(new int[]{1,2,3}) vira Stream<int[]> de 1 elemento.
        System.out.println("Stream.of(int[]).count()   = " + Stream.of(new int[]{1, 2, 3}).count()); // 1 (!)

        // 3) Streams retornados dentro do flatMap são fechados automaticamente
        //    pelo próprio flatMap depois de consumidos — por isso o Files.lines
        //    do exemplo acima não vaza file handle.

        // 4) flatMap consome o stream interno: não dá pra reutilizá-lo.
        Stream<String> interno = Stream.of("x");
        List<String> ok = Stream.of(1).flatMap(i -> interno).toList();
        System.out.println("primeiro uso: " + ok);
        try {
            Stream.of(1).flatMap(i -> interno).toList();
        } catch (IllegalStateException e) {
            System.out.println("segundo uso: " + e.getMessage());
        }

        // 5) Java 16+ tem mapMulti: alternativa ao flatMap sem alocar um Stream
        //    por elemento. Mais rápido em caminhos quentes, menos legível.
        List<String> comMapMulti = List.of(List.of("a", "b"), List.of("c")).stream()
                .<String>mapMulti((lista, consumer) -> lista.forEach(consumer))
                .toList();
        System.out.println("mapMulti: " + comMapMulti);
    }
}
