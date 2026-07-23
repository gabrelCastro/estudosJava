import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * 2) Como tirar o valor de dentro do Optional.
 *
 * Esta é a parte onde mais se erra: muita gente usa isPresent() + get(),
 * que é literalmente reescrever o "if != null" com mais cerimônia.
 */
public class ConsumindoOptional {

    public static void main(String[] args) {
        Optional<String> cheio = Optional.of("Java");
        Optional<String> vazio = Optional.empty();

        // ------------------------------------------------------------------
        // orElse: valor alternativo já pronto
        // ------------------------------------------------------------------
        System.out.println("orElse cheio -> " + cheio.orElse("padrão"));
        System.out.println("orElse vazio -> " + vazio.orElse("padrão"));

        // ------------------------------------------------------------------
        // orElseGet: valor alternativo calculado sob demanda (Supplier)
        // ------------------------------------------------------------------
        System.out.println("\n--- orElse vs orElseGet ---");
        // ARMADILHA: o argumento do orElse é avaliado SEMPRE, mesmo quando o
        // Optional tem valor e a alternativa será descartada.
        System.out.print("orElse    com valor presente: ");
        cheio.orElse(caro());        // executa caro() à toa!

        System.out.print("orElseGet com valor presente: ");
        cheio.orElseGet(ConsumindoOptional::caro); // não executa nada
        System.out.println("(nada foi executado)");

        // Se a alternativa é uma constante, orElse.
        // Se envolve cálculo, I/O ou criação de objeto, orElseGet.

        // ------------------------------------------------------------------
        // orElseThrow: exige o valor
        // ------------------------------------------------------------------
        System.out.println("\n--- orElseThrow ---");
        try {
            vazio.orElseThrow(() -> new IllegalStateException("usuário não encontrado"));
        } catch (IllegalStateException e) {
            System.out.println("com exceção customizada: " + e.getMessage());
        }

        try {
            vazio.orElseThrow(); // sem argumento (Java 10+): NoSuchElementException
        } catch (NoSuchElementException e) {
            System.out.println("sem argumento: " + e.getMessage());
        }

        // ------------------------------------------------------------------
        // get(): evite
        // ------------------------------------------------------------------
        // Faz o mesmo que orElseThrow() sem argumento, mas o nome não avisa
        // que pode explodir. Está marcado para depreciação justamente por isso.
        // Se você "sabe" que tem valor, use orElseThrow() — deixa a intenção clara.

        // ------------------------------------------------------------------
        // ifPresent / ifPresentOrElse: quando não precisa devolver nada
        // ------------------------------------------------------------------
        System.out.println("\n--- efeitos colaterais ---");
        cheio.ifPresent(v -> System.out.println("ifPresent: achei " + v));
        vazio.ifPresent(v -> System.out.println("nunca imprime"));

        // Java 9+: os dois caminhos
        vazio.ifPresentOrElse(
                v -> System.out.println("achei " + v),
                () -> System.out.println("ifPresentOrElse: não achei nada"));

        // ------------------------------------------------------------------
        // isPresent / isEmpty: último recurso
        // ------------------------------------------------------------------
        System.out.println("\n--- o antipadrão ---");

        // RUIM: só trocou o null por Optional, a estrutura continua igual.
        if (cheio.isPresent()) {
            System.out.println("ruim: " + cheio.get().toUpperCase());
        } else {
            System.out.println("ruim: vazio");
        }

        // BOM: descreve a transformação, sem desempacotar no meio do caminho.
        System.out.println("bom : " + cheio.map(String::toUpperCase).orElse("vazio"));

        // isPresent()/isEmpty() são legítimos quando você só quer o boolean:
        long quantos = java.util.stream.Stream.of(cheio, vazio)
                .filter(Optional::isPresent)
                .count();
        System.out.println("quantos preenchidos: " + quantos);

        // ------------------------------------------------------------------
        // Tabela de decisão
        // ------------------------------------------------------------------
        System.out.println("""

                quero...                              use
                ------------------------------------  ---------------------
                um padrão constante                   orElse(x)
                um padrão caro de calcular            orElseGet(() -> x)
                falhar se não tiver                   orElseThrow(...)
                executar algo só se tiver             ifPresent(...)
                dois caminhos                         ifPresentOrElse(...)
                transformar o conteúdo                map / flatMap
                só saber se tem                       isPresent / isEmpty
                """);
    }

    /** Simula uma alternativa cara (consulta a banco, chamada HTTP, etc.). */
    static String caro() {
        System.out.println("<< caro() foi executado >>");
        return "padrão caro";
    }
}
