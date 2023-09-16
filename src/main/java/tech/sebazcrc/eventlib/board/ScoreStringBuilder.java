package tech.sebazcrc.eventlib.board;

public class ScoreStringBuilder {

    private String currentBuild;
    private boolean signature;

    public ScoreStringBuilder(boolean signature) {
        this.currentBuild = " ";
        this.signature = signature;
    }

    public ScoreStringBuilder add(String s) {
        this.currentBuild = currentBuild + "\n" + s;
        return this;
    }

    public ScoreStringBuilder addUnFormatted(String s) {
        this.currentBuild = currentBuild + "\n" + s;
        return this;
    }

    public ScoreStringBuilder space() {
        return add(" ");
    }

    public String build() {
        if (signature) add("&ewolfcraft.live");
        return this.currentBuild;
    }
}
