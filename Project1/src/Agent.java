import java.util.ArrayList;
import java.util.Map;

/**
 * Your Agent for solving Raven's Progressive Matrices. You MUST modify this
 * file.
 * <p/>
 * You may also create and submit new files in addition to modifying this file.
 * <p/>
 * Make sure your file retains methods with the signatures:
 * public Agent()
 * public char Solve(RavensProblem problem)
 * <p/>
 * These methods will be necessary for the project's main method to run.
 */
public class Agent {
    ArrayList<Transformation> sourceTransformations;

    /**
     * The default constructor for your Agent. Make sure to execute any
     * processing necessary before your Agent starts solving problems here.
     * <p/>
     * Do not add any variables to this signature; they will not be used by
     * main().
     */
    public Agent() {
        sourceTransformations = new ArrayList<Transformation>();
    }

    /**
     * The primary method for solving incoming Raven's Progressive Matrices.
     * For each problem, your Agent's Solve() method will be called. At the
     * conclusion of Solve(), your Agent should return a String representing its
     * answer to the question: "1", "2", "3", "4", "5", or "6". These Strings
     * are also the Names of the individual RavensFigures, obtained through
     * RavensFigure.getName().
     * <p/>
     * In addition to returning your answer at the end of the method, your Agent
     * may also call problem.checkAnswer(String givenAnswer). The parameter
     * passed to checkAnswer should be your Agent's current guess for the
     * problem; checkAnswer will return the correct answer to the problem. This
     * allows your Agent to check its answer. Note, however, that after your
     * agent has called checkAnswer, it will *not* be able to change its answer.
     * checkAnswer is used to allow your Agent to learn from its incorrect
     * answers; however, your Agent cannot change the answer to a question it
     * has already answered.
     * <p/>
     * If your Agent calls checkAnswer during execution of Solve, the answer it
     * returns will be ignored; otherwise, the answer returned at the end of
     * Solve will be taken as your Agent's answer to this problem.
     *
     * @param problem the RavensProblem your agent should solve
     * @return your Agent's answer to this problem
     */
    public String Solve(RavensProblem problem) {

        int source_diff = this.getFigureDifference(problem.getFigures().get("A"), problem.getFigures().get("B"));

//        if (problem.getName().compareTo("2x1 Basic Problem 12") != 0)
//            return "0";

        // Figure out transformation between A and B first
        RavensFigure A = problem.getFigures().get("A");
        RavensFigure B = problem.getFigures().get("B");
        RavensFigure C = problem.getFigures().get("C");

        ArrayList<Transformation> sourceTransformations = computeTransformation(A, B);

        System.out.println(A.getName() + " ---> " + B.getName());
        printTransformations(sourceTransformations);

        Map<String, RavensFigure> map = problem.getFigures();
        int lowest_diff = 9999;
        ArrayList<RavensFigure> candidateAnswers = new ArrayList<RavensFigure>();
        for (RavensFigure candidateFigure : map.values()) {
            // Skip A, B, and C
            if (candidateFigure.getName().compareTo("A") != 0 && candidateFigure.getName().compareTo("B") != 0 && candidateFigure.getName().compareTo("C") != 0) {

                ArrayList<Transformation> candidateTransformations = computeTransformation(C, candidateFigure);

                ArrayList<RavensAttribute> sourceMutationsCollection = collectMutations(sourceTransformations);
                ArrayList<RavensAttribute> candidateMutationsCollection = collectMutations(candidateTransformations);

                int diff = 0;
                int number_found = 0;
                for (RavensAttribute sourceMutation : sourceMutationsCollection) {
                    boolean found = false;
                    for (RavensAttribute candidateMutation : candidateMutationsCollection) {
                        if (sourceMutation.getName().compareTo(candidateMutation.getName()) == 0) {
//                            if(sourceMutation.getValue().compareTo(candidateMutation.getValue()) == 0) {
                                found = true;
                                number_found++;
//                            }
                        }
                    }

                    if (!found)
                        diff++;
                }

                if (number_found < candidateMutationsCollection.size())
                    diff +=  number_found;
                else if(number_found <= candidateMutationsCollection.size() && sourceMutationsCollection.size() < candidateMutationsCollection.size()){
                    diff +=  number_found;
                }

                if (diff < lowest_diff) {
                    lowest_diff = diff;
                    candidateAnswers.clear();
                    candidateAnswers.add(candidateFigure);
                } else if (diff == lowest_diff) {
                    candidateAnswers.add(candidateFigure);
                }

                System.out.println(C.getName() + " ---> " + candidateFigure.getName());
                printTransformations(candidateTransformations);
            }

        }

        if (candidateAnswers.size() == 1)
            return candidateAnswers.get(0).getName();
        else {
            RavensFigure answer = null;
            for (RavensFigure candidateFigure : candidateAnswers) {
                if (candidateFigure.getObjects().get(0).getAttributes().get(0).getValue().compareTo(C.getObjects().get(0).getAttributes().get(0).getValue()) == 0) {
                    answer = candidateFigure;
                }
            }

            return answer.getName();
        }

    }

    public RavensFigure applyTransformation(ArrayList<Transformation> sourceTransformations, RavensFigure figure) {
        RavensFigure transformedFigure = new RavensFigure(figure.getName() + "*");
        for (Transformation sourceTransformation : sourceTransformations) {
            String test = "test";
        }

        return null;
    }


    public void asdf() {

    }
    public ArrayList<RavensAttribute> collectMutations(ArrayList<Transformation> transformations) {
        ArrayList<RavensAttribute> mutations = new ArrayList<RavensAttribute>();

        for (Transformation transformation : transformations) {
            // Introduce a 'none' mutation to indicate no change
            if (transformation.getMutations().isEmpty()) {
                mutations.add(new RavensAttribute("none", "none"));
            }
            for (RavensAttribute mutation : transformation.getMutations()) {
                mutations.add(mutation);
            }
        }

        return mutations;
    }

    public void printTransformations(ArrayList<Transformation> transformation) {
        for (Transformation trans : transformation) {
            trans.print();
        }
    }

    private ArrayList<Transformation> computeTransformation(RavensFigure figure1, RavensFigure figure2) {
        ArrayList<Transformation> transformations = new ArrayList<Transformation>();
        ArrayList<RavensObject> targetsMapped = new ArrayList<RavensObject>();

        for (RavensObject objectInA : figure1.getObjects()) {
            Transformation transformation = new Transformation();
            transformation.setSource(objectInA);

            int lowest_diff = 9999;
            for (RavensObject objectInB : figure2.getObjects()) {
                // If object is already mapped, don't do anything
                if (targetsMapped.contains(objectInB))
                    continue;

                int diff = getDifferences(objectInA.getAttributes(), objectInB.getAttributes());

                if (diff < lowest_diff) {
                    lowest_diff = diff;
                    transformation.setTarget(objectInB);
                    targetsMapped.add(objectInB);
                }
            }

            if (transformation.getTarget() != null) {
                transformation.computeMutations();
                transformations.add(transformation);
            }

            if(figure2.getObjects().size() >= figure1.getObjects().size())
                targetsMapped.clear();

        }

        return transformations;
    }

    /**
     * Assumes a 1-1 attribute mapping
     * Merges the name of the attribute with the value. Like "shape:circle"
     *
     * @param source
     * @param target
     * @return
     */
    public int getDifferences(ArrayList<RavensAttribute> source, ArrayList<RavensAttribute> target) {
        int differences = 0;
        for (RavensAttribute sourceAttribute : source) {
            boolean found = false;
            for (RavensAttribute targetAttribute : target) {
                if (sourceAttribute.getName().compareTo(targetAttribute.getName()) == 0) {
                    found = true;
                    if (sourceAttribute.getValue().compareTo(targetAttribute.getValue()) != 0) {
                        differences++;
                    }
                }
            }

            if (!found) {
                differences++;
            }
        }

        return differences;
    }

    /**
     * Calculates the difference between 2 figures
     *
     * @param figure1
     * @param figure2
     * @return
     */
    private int getFigureDifference(RavensFigure figure1, RavensFigure figure2) {
        int diff = 0;
        for (RavensObject object1 : figure1.getObjects()) {
            // Are there multiple similar shapes in figure 2?
            if (numberOfShapesLike(figure2, object1) > 1) {
                // Find the shortest path from object1 to those similar shapes to figure out which one is correct

            }
            for (RavensObject object2 : figure2.getObjects()) {
                if (isSameObject(object1, object2)) {
                    diff += this.getObjectDifference(object1, object2);
                }
            }
        }

        return diff;
    }

    /**
     * Calculates the difference between 2 objects
     *
     * @param object1
     * @param object2
     * @return
     */
    private int getObjectDifference(RavensObject object1, RavensObject object2) {
        int diff = 0;
        for (RavensAttribute attribute1 : object1.getAttributes()) {
            for (RavensAttribute attribute2 : object2.getAttributes()) {
                diff += this.getAttributeDifference(attribute1, attribute2);
            }
        }

        return diff;
    }

    /**
     * Check whether two attributes are the same. Only attributes with the same name are comparable
     *
     * @param attribute1
     * @param attribute2
     * @return
     */
    private int getAttributeDifference(RavensAttribute attribute1, RavensAttribute attribute2) {
        if (attribute1.getName().compareTo(attribute2.getName()) == 0) // Comparing only same attributes
            if (attribute1.getValue().compareTo(attribute2.getValue()) != 0)
                return 1;

        return 0;
    }

    /**
     * Checks whether these to objects are the same shape
     * Makes the assumption that the first attribute is the shape type
     *
     * @param object1
     * @param object2
     * @return
     */
    private boolean isSameObject(RavensObject object1, RavensObject object2) {
        return object1.getAttributes().get(0).getValue().compareTo(object2.getAttributes().get(0).getValue()) == 0;
    }

    /**
     * Check if target figure has multiple of given object
     *
     * @param targetFigure
     * @param needle
     * @return
     */
    private int numberOfShapesLike(RavensFigure targetFigure, RavensObject needle) {
        int shapesFound = 0;
        for (RavensObject object : targetFigure.getObjects()) {
            if (getShapeName(object).compareTo(getShapeName(needle)) == 0) {
                shapesFound++;
            }
        }

        return shapesFound;
    }

    private String getShapeName(RavensObject object) {
        for (RavensAttribute attribute : object.getAttributes()) {
            if (attribute.getName().compareTo("shape") == 0) {
                return attribute.getValue();
            }
        }

        return null;
    }

}
