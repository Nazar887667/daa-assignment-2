"""
Generates all plots required by Assignment 2.

Reads CSV files from results/tables
and saves PNG plots to results/plots.
"""

import csv
import os
import matplotlib.pyplot as plt


TABLES = os.path.join(
    os.path.dirname(__file__), "..", "results", "tables"
)

PLOTS = os.path.join(
    os.path.dirname(__file__), "..", "results", "plots"
)

os.makedirs(PLOTS, exist_ok=True)


def read_csv(name):
    path = os.path.join(TABLES, name)

    with open(path, newline="") as file:
        return list(csv.DictReader(file))


def save(fig, name):
    fig.tight_layout()
    fig.savefig(
        os.path.join(PLOTS, name),
        dpi=150
    )
    plt.close(fig)


# Workload 1: Random Access

rows = read_csv("workload1_random_access.csv")
ns = sorted(set(int(r["n"]) for r in rows))

fig, ax = plt.subplots()

for structure in ["DynamicArray", "LinkedList"]:
    times = [
        float(r["avg_time_ns"]) / 1e6
        for r in rows
        if r["structure"] == structure
    ]

    ax.plot(
        ns,
        times,
        marker="o",
        label=structure
    )

ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlabel("n")
ax.set_ylabel("Average execution time (ms)")
ax.set_title("Workload 1 - Random Access: Time vs. n")
ax.legend()
ax.grid(True, which="both", alpha=0.3)

save(fig, "workload1_time_vs_n.png")


fig, ax = plt.subplots()

for structure in ["DynamicArray", "LinkedList"]:
    accesses = [
        int(r["accesses"])
        for r in rows
        if r["structure"] == structure
    ]

    ax.plot(
        ns,
        accesses,
        marker="o",
        label=structure
    )

ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlabel("n")
ax.set_ylabel("Element accesses")
ax.set_title("Workload 1 - Random Access: Accesses vs. n")
ax.legend()
ax.grid(True, which="both", alpha=0.3)

save(fig, "workload1_accesses_vs_n.png")


# Workload 2: Search

rows = read_csv("workload2_search.csv")
ns = sorted(set(int(r["n"]) for r in rows))

fig, ax = plt.subplots()

for structure in ["DynamicArray", "LinkedList"]:
    times = [
        float(r["avg_time_ns"]) / 1e6
        for r in rows
        if r["structure"] == structure
    ]

    ax.plot(
        ns,
        times,
        marker="o",
        label=structure
    )

ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlabel("n")
ax.set_ylabel("Average execution time (ms)")
ax.set_title("Workload 2 - Search: Time vs. n")
ax.legend()
ax.grid(True, which="both", alpha=0.3)

save(fig, "workload2_time_vs_n.png")


fig, ax = plt.subplots()

for structure in ["DynamicArray", "LinkedList"]:
    comparisons = [
        int(r["comparisons"])
        for r in rows
        if r["structure"] == structure
    ]

    ax.plot(
        ns,
        comparisons,
        marker="o",
        label=structure
    )

ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlabel("n")
ax.set_ylabel("Element comparisons")
ax.set_title("Workload 2 - Search: Comparisons vs. n")
ax.legend()
ax.grid(True, which="both", alpha=0.3)

save(fig, "workload2_comparisons_vs_n.png")


# Workload 3: Insertion and Removal

rows = read_csv("workload3_insertion_removal.csv")
ns = sorted(set(int(r["n"]) for r in rows))

for position in ["beginning", "middle"]:

    fig, ax = plt.subplots()

    for structure in ["DynamicArray", "LinkedList"]:

        for operation, style in [
            ("insert", "-o"),
            ("remove", "--s")
        ]:

            times = [
                float(r["avg_time_ns"]) / 1e6
                for r in rows
                if (
                    r["structure"] == structure
                    and r["position"] == position
                    and r["operation"] == operation
                )
            ]

            ax.plot(
                ns,
                times,
                style,
                label=f"{structure} {operation}"
            )

    ax.set_xscale("log")
    ax.set_yscale("log")
    ax.set_xlabel("n")
    ax.set_ylabel("Average execution time (ms)")
    ax.set_title(
        f"Workload 3 - Insertion/Removal at {position}"
    )
    ax.legend(fontsize=8)
    ax.grid(True, which="both", alpha=0.3)

    save(
        fig,
        f"workload3_time_vs_n_{position}.png"
    )


    fig, ax = plt.subplots()

    for structure in ["DynamicArray", "LinkedList"]:

        for operation, style in [
            ("insert", "-o"),
            ("remove", "--s")
        ]:

            movements = [
                int(r["movements"])
                for r in rows
                if (
                    r["structure"] == structure
                    and r["position"] == position
                    and r["operation"] == operation
                )
            ]

            ax.plot(
                ns,
                movements,
                style,
                label=f"{structure} {operation}"
            )

    ax.set_xscale("log")
    ax.set_yscale("log")
    ax.set_xlabel("n")
    ax.set_ylabel("Element movements")
    ax.set_title(
        f"Workload 3 - Movements at {position}"
    )
    ax.legend(fontsize=8)
    ax.grid(True, which="both", alpha=0.3)

    save(
        fig,
        f"workload3_movements_vs_n_{position}.png"
    )


# Workload 4: Priority Processing

rows = read_csv(
    "workload4_priority_processing.csv"
)

ns = sorted(set(int(r["n"]) for r in rows))


fig, ax = plt.subplots()

insert_times = [
    float(r["avg_insert_time_ns"]) / 1e6
    for r in rows
]

extract_times = [
    float(r["avg_extract_time_ns"]) / 1e6
    for r in rows
]

ax.plot(
    ns,
    insert_times,
    marker="o",
    label="insert"
)

ax.plot(
    ns,
    extract_times,
    marker="s",
    label="extractMin"
)

ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlabel("n")
ax.set_ylabel("Average total execution time (ms)")
ax.set_title(
    "Workload 4 - Priority Processing: Time vs. n"
)
ax.legend()
ax.grid(True, which="both", alpha=0.3)

save(
    fig,
    "workload4_time_vs_n.png"
)


fig, ax = plt.subplots()

insert_comparisons = [
    int(r["insert_comparisons"])
    for r in rows
]

extract_comparisons = [
    int(r["extract_comparisons"])
    for r in rows
]

ax.plot(
    ns,
    insert_comparisons,
    marker="o",
    label="insert comparisons"
)

ax.plot(
    ns,
    extract_comparisons,
    marker="s",
    label="extractMin comparisons"
)

ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlabel("n")
ax.set_ylabel("Comparisons")
ax.set_title(
    "Workload 4 - Priority Processing: Comparisons vs. n"
)
ax.legend()
ax.grid(True, which="both", alpha=0.3)

save(
    fig,
    "workload4_comparisons_vs_n.png"
)


print("All plots written to:", PLOTS)